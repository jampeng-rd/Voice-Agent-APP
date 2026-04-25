package com.jam.voiceagent.data.repository

import com.google.gson.Gson
import com.jam.voiceagent.data.local.SessionStore
import com.jam.voiceagent.data.local.TokenStore
import com.jam.voiceagent.data.model.ChatRequest
import com.jam.voiceagent.data.model.ChatResponse
import com.jam.voiceagent.data.network.ChatApi
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
            onFailure = {
                ChatSendResult(errorMessage = "目前連線有點問題，請稍後再試。")
            }
        )
    }

    fun clearSession() {
        sessionStore.clearSessionId()
    }

    private fun handleChatResponse(response: Response<ChatResponse>): ChatSendResult {
        if (response.isSuccessful) {
            val body = response.body()
            return if (body?.success == true && !body.ai_reply.isNullOrBlank()) {
                ChatSendResult(aiReply = body.ai_reply)
            } else {
                ChatSendResult(errorMessage = body?.error_message ?: "目前連線有點問題，請稍後再試。")
            }
        }

        val parsedError = parseErrorMessage(response)
        val requiresLogin = response.code() == 401
        return ChatSendResult(
            errorMessage = parsedError ?: if (requiresLogin) "登入狀態已失效，請重新登入。" else "目前連線有點問題，請稍後再試。",
            requiresLogin = requiresLogin
        )
    }

    private fun parseErrorMessage(response: Response<ChatResponse>): String? {
        val raw = response.errorBody()?.string().orEmpty()
        if (raw.isBlank()) return null
        return runCatching { gson.fromJson(raw, ChatResponse::class.java)?.error_message }.getOrNull()
    }
}
