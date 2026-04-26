package com.jam.voiceagent.data.repository

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.jam.voiceagent.data.local.SessionStore
import com.jam.voiceagent.data.local.TokenStore
import com.jam.voiceagent.data.model.ChatRequest
import com.jam.voiceagent.data.model.ChatResponse
import com.jam.voiceagent.data.network.ChatApi
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import retrofit2.Response

data class ChatSendResult(
    val aiReply: String? = null,
    val errorMessage: String? = null,
    val requiresLogin: Boolean = false
) {
    val isSuccess: Boolean = !aiReply.isNullOrBlank() && errorMessage == null
}

class ChatRepository(
    private val chatApi: ChatApi,
    private val tokenStore: TokenStore,
    private val sessionStore: SessionStore
) {
    private val gson = Gson()

    suspend fun sendText(text: String): ChatSendResult {
        val token = tokenStore.getToken()
        if (token.isNullOrBlank()) {
            return ChatSendResult(
                errorMessage = "請先登入後再試。",
                requiresLogin = true
            )
        }

        val sessionId = sessionStore.getOrCreateSessionId()
        return runCatching {
            chatApi.sendText(
                authorization = "Bearer $token",
                request = ChatRequest(
                    session_id = sessionId,
                    text = text
                )
            )
        }.fold(
            onSuccess = { response ->
                handleChatResponse(response)
            },
            onFailure = { throwable ->
                val (uiMessage, type) = classifyThrowable(throwable)
                Log.w(TAG, "chat send failed: type=$type sessionId=$sessionId message=${throwable.message}")
                ChatSendResult(errorMessage = uiMessage)
            }
        )
    }

    fun clearSession() {
        sessionStore.clearSessionId()
    }

    private fun handleChatResponse(response: Response<ChatResponse>): ChatSendResult {
        if (response.isSuccessful) {
            val body = runCatching { response.body() }.getOrElse { parseError ->
                Log.w(TAG, "chat response parse failed on success body: message=${parseError.message}")
                return ChatSendResult(errorMessage = "回覆解析失敗，請稍後再試。")
            }

            if (body?.success == true) {
                val reply = body.ai_reply?.takeIf { it.isNotBlank() } ?: "已收到回覆。"
                return ChatSendResult(aiReply = reply)
            }

            Log.w(
                TAG,
                "chat response unsuccessful flag in 2xx: code=${response.code()} success=${body?.success} error=${body?.error_message}"
            )
            return ChatSendResult(errorMessage = body?.error_message ?: "目前連線有點問題，請稍後再試。")
        }

        val parsedError = parseErrorMessage(response)
        val code = response.code()
        val requiresLogin = code == 401
        val type = when {
            code == 401 -> "unauthorized"
            code in 400..499 -> "client_error"
            code >= 500 -> "server_error"
            else -> "http_error"
        }
        Log.w(TAG, "chat response failed: type=$type code=$code error=$parsedError")

        val uiMessage = when {
            requiresLogin -> parsedError ?: "登入狀態已失效，請重新登入。"
            code in 400..499 -> parsedError ?: "請求格式或內容有誤，請稍後再試。"
            code >= 500 -> parsedError ?: "伺服器忙碌中，請稍後再試。"
            else -> parsedError ?: "目前連線有點問題，請稍後再試。"
        }
        return ChatSendResult(
            errorMessage = uiMessage,
            requiresLogin = requiresLogin
        )
    }

    private fun parseErrorMessage(response: Response<ChatResponse>): String? {
        val raw = response.errorBody()?.string().orEmpty()
        if (raw.isBlank()) return null
        return runCatching { gson.fromJson(raw, ChatResponse::class.java)?.error_message }.getOrNull()
    }

    private fun classifyThrowable(throwable: Throwable): Pair<String, String> {
        return when (throwable) {
            is SocketTimeoutException,
            is InterruptedIOException -> "伺服器回應較慢，請稍後再試。" to "timeout"

            is UnknownHostException,
            is ConnectException -> "目前無法連線到伺服器，請確認網路與伺服器狀態。" to "network_unreachable"

            is JsonSyntaxException -> "回覆解析失敗，請稍後再試。" to "response_parse_error"
            else -> "目前連線有點問題，請稍後再試。" to "unknown_error"
        }
    }

    companion object {
        private const val TAG = "ChatRepository"
    }
}
