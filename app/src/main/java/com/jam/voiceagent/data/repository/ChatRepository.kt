package com.jam.voiceagent.data.repository

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.jam.voiceagent.data.local.SessionStore
import com.jam.voiceagent.data.local.TokenStore
import com.jam.voiceagent.data.model.ChatRequest
import com.jam.voiceagent.data.model.ChatResponse
import com.jam.voiceagent.data.network.ChatApi
import com.jam.voiceagent.data.util.SessionDebug
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import retrofit2.Response

data class ChatSendResult(
    val aiReply: String? = null,
    val errorMessage: String? = null,
    val switchedToGuest: Boolean = false
) {
    val isSuccess: Boolean = !aiReply.isNullOrBlank() && errorMessage == null
}

class ChatRepository(
    private val chatApi: ChatApi,
    private val tokenStore: TokenStore,
    private val sessionStore: SessionStore,
    private val authRepository: AuthRepository
) {
    private val gson = Gson()

    suspend fun sendText(text: String): ChatSendResult {
        return sendTextInternal(text = text, hasRetriedAfterRecovery = false)
    }

    private suspend fun sendTextInternal(
        text: String,
        hasRetriedAfterRecovery: Boolean
    ): ChatSendResult {
        val usingRegistered = tokenStore.isUsingRegisteredToken()
        val identity = if (usingRegistered) "registered" else "guest"
        val token = if (usingRegistered) {
            tokenStore.getRegisteredToken()
        } else {
            tokenStore.getGuestToken()
        }
        if (token.isNullOrBlank()) {
            return ChatSendResult(
                errorMessage = "目前無法建立訪客連線，請稍後再試。"
            )
        }

        val sessionId = sessionStore.getOrCreateSessionId(isRegistered = usingRegistered)
        Log.i(
            TAG,
            "chat request: identity=$identity session=${SessionDebug.short(sessionId)} retry=$hasRetriedAfterRecovery"
        )
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
                handleChatResponse(
                    response = response,
                    originalText = text,
                    usingRegistered = usingRegistered,
                    hasRetriedAfterRecovery = hasRetriedAfterRecovery,
                    requestSessionId = sessionId
                )
            },
            onFailure = { throwable ->
                val (uiMessage, type) = classifyThrowable(throwable)
                val identity = if (usingRegistered) "registered" else "guest"
                Log.w(
                    TAG,
                    "chat send failed: type=$type identity=$identity retry=$hasRetriedAfterRecovery message=${throwable.message}"
                )
                ChatSendResult(errorMessage = uiMessage)
            }
        )
    }

    fun clearSession() {
        sessionStore.clearAllSessionIds()
    }

    private suspend fun handleChatResponse(
        response: Response<ChatResponse>,
        originalText: String,
        usingRegistered: Boolean,
        hasRetriedAfterRecovery: Boolean,
        requestSessionId: String
    ): ChatSendResult {
        if (response.isSuccessful) {
            val body = runCatching { response.body() }.getOrElse { parseError ->
                Log.w(TAG, "chat response parse failed on success body: message=${parseError.message}")
                return ChatSendResult(errorMessage = "回覆解析失敗，請稍後再試。")
            }

            if (body?.success == true) {
                val responseSessionId = body.session_id
                Log.i(
                    TAG,
                    "chat response success: identity=${if (usingRegistered) "registered" else "guest"} request=${SessionDebug.short(requestSessionId)} response=${SessionDebug.short(responseSessionId)}"
                )
                if (!responseSessionId.isNullOrBlank() && responseSessionId != requestSessionId) {
                    Log.w(
                        TAG,
                        "chat response session differs from request: request=${SessionDebug.short(requestSessionId)} response=${SessionDebug.short(responseSessionId)}"
                    )
                }
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
        val identity = if (usingRegistered) "registered" else "guest"
        val type = when {
            code == 401 -> "unauthorized"
            code in 400..499 -> "client_error"
            code >= 500 -> "server_error"
            else -> "http_error"
        }
        Log.w(TAG, "chat response failed: type=$type code=$code identity=$identity retry=$hasRetriedAfterRecovery")

        if (code == 401 && !hasRetriedAfterRecovery) {
            Log.i(TAG, "chat token recovery start: identity=$identity")
            val recoveryResult = if (usingRegistered) {
                authRepository.refreshRegisteredToken()
            } else {
                authRepository.recreateGuestTokenIfNeeded()
            }
            if (recoveryResult.isSuccess) {
                Log.i(TAG, "chat token recovery success: identity=$identity retry=true")
                return sendTextInternal(
                    text = originalText,
                    hasRetriedAfterRecovery = true
                )
            }
            Log.w(TAG, "chat token recovery failed: identity=$identity switchedToGuest=${recoveryResult.switchedToGuest}")
            return ChatSendResult(
                errorMessage = recoveryResult.errorMessage
                    ?: if (usingRegistered) "登入已過期，已切換為訪客模式。" else "目前無法建立訪客連線，請稍後再試。",
                switchedToGuest = recoveryResult.switchedToGuest
            )
        }

        val uiMessage = when {
            code == 401 -> parsedError ?: "目前無法建立訪客連線，請稍後再試。"
            code in 400..499 -> parsedError ?: "請求格式或內容有誤，請稍後再試。"
            code >= 500 -> parsedError ?: "伺服器忙碌中，請稍後再試。"
            else -> parsedError ?: "目前連線有點問題，請稍後再試。"
        }
        return ChatSendResult(errorMessage = uiMessage)
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
