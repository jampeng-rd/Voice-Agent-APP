package com.jam.voiceagent.data.repository

import android.util.Log
import com.google.gson.Gson
import com.jam.voiceagent.data.local.TokenStore
import com.jam.voiceagent.data.model.ConversationDeleteResponse
import com.jam.voiceagent.data.model.ConversationDetail
import com.jam.voiceagent.data.model.ConversationDetailResponse
import com.jam.voiceagent.data.model.ConversationListResponse
import com.jam.voiceagent.data.model.ConversationMessage
import com.jam.voiceagent.data.model.ConversationSummary
import com.jam.voiceagent.data.network.ConversationApi
import retrofit2.Response

data class ConversationListResult(
    val conversations: List<ConversationSummary> = emptyList(),
    val errorMessage: String? = null,
    val switchedToGuest: Boolean = false,
    val requiresLogin: Boolean = false
) {
    val isSuccess: Boolean = errorMessage == null
}

data class ConversationDetailResult(
    val detail: ConversationDetail? = null,
    val errorMessage: String? = null,
    val switchedToGuest: Boolean = false,
    val requiresLogin: Boolean = false
) {
    val isSuccess: Boolean = detail != null && errorMessage == null
}

data class ConversationDeleteResult(
    val isDeleted: Boolean = false,
    val errorMessage: String? = null,
    val switchedToGuest: Boolean = false,
    val requiresLogin: Boolean = false
) {
    val isSuccess: Boolean = isDeleted && errorMessage == null
}

class ConversationRepository(
    private val conversationApi: ConversationApi,
    private val tokenStore: TokenStore,
    private val authRepository: AuthRepository
) {
    private val gson = Gson()

    suspend fun getConversations(): ConversationListResult {
        return getConversationsInternal(hasRetriedAfterRecovery = false)
    }

    suspend fun getConversationDetail(sessionId: String): ConversationDetailResult {
        if (sessionId.isBlank()) {
            return ConversationDetailResult(errorMessage = "無效的對話識別。")
        }
        return getConversationDetailInternal(sessionId = sessionId, hasRetriedAfterRecovery = false)
    }

    suspend fun deleteConversation(sessionId: String): ConversationDeleteResult {
        if (sessionId.isBlank()) {
            return ConversationDeleteResult(errorMessage = "無效的對話識別。")
        }
        return deleteConversationInternal(sessionId = sessionId, hasRetriedAfterRecovery = false)
    }

    private suspend fun getConversationsInternal(hasRetriedAfterRecovery: Boolean): ConversationListResult {
        val token = tokenStore.getRegisteredToken()
        if (token.isNullOrBlank()) {
            return ConversationListResult(
                errorMessage = LOGIN_REQUIRED_MESSAGE,
                requiresLogin = true
            )
        }

        val response = runCatching {
            conversationApi.getConversations(authorization = "Bearer $token")
        }.getOrElse {
            Log.w(TAG, "conversation list request failed: retry=$hasRetriedAfterRecovery")
            return ConversationListResult(errorMessage = "目前連線有點問題，請稍後再試。")
        }

        return handleListResponse(response, hasRetriedAfterRecovery)
    }

    private suspend fun getConversationDetailInternal(
        sessionId: String,
        hasRetriedAfterRecovery: Boolean
    ): ConversationDetailResult {
        val token = tokenStore.getRegisteredToken()
        if (token.isNullOrBlank()) {
            return ConversationDetailResult(
                errorMessage = LOGIN_REQUIRED_MESSAGE,
                requiresLogin = true
            )
        }

        val response = runCatching {
            conversationApi.getConversationDetail(
                authorization = "Bearer $token",
                sessionId = sessionId
            )
        }.getOrElse {
            Log.w(TAG, "conversation detail request failed: retry=$hasRetriedAfterRecovery")
            return ConversationDetailResult(errorMessage = "目前連線有點問題，請稍後再試。")
        }

        return handleDetailResponse(response, sessionId, hasRetriedAfterRecovery)
    }

    private suspend fun deleteConversationInternal(
        sessionId: String,
        hasRetriedAfterRecovery: Boolean
    ): ConversationDeleteResult {
        val token = tokenStore.getRegisteredToken()
        if (token.isNullOrBlank()) {
            return ConversationDeleteResult(
                errorMessage = LOGIN_REQUIRED_MESSAGE,
                requiresLogin = true
            )
        }

        val response = runCatching {
            conversationApi.deleteConversation(
                authorization = "Bearer $token",
                sessionId = sessionId
            )
        }.getOrElse {
            Log.w(TAG, "conversation delete request failed: retry=$hasRetriedAfterRecovery")
            return ConversationDeleteResult(errorMessage = "目前連線有點問題，請稍後再試。")
        }

        return handleDeleteResponse(response, sessionId, hasRetriedAfterRecovery)
    }

    private suspend fun handleListResponse(
        response: Response<ConversationListResponse>,
        hasRetriedAfterRecovery: Boolean
    ): ConversationListResult {
        if (response.isSuccessful) {
            val body = response.body()
            if (body?.success == true) {
                val conversations = body.conversations.orEmpty().mapNotNull { item ->
                    val sessionId = item.session_id?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                    ConversationSummary(
                        sessionId = sessionId,
                        title = item.title,
                        createdAt = item.created_at,
                        updatedAt = item.updated_at
                    )
                }
                return ConversationListResult(conversations = conversations)
            }
            return ConversationListResult(errorMessage = body?.error_message ?: "載入歷史對話失敗，請稍後再試。")
        }

        return when (response.code()) {
            401 -> recoverAndRetryListIfNeeded(hasRetriedAfterRecovery)
            403 -> ConversationListResult(
                errorMessage = LOGIN_REQUIRED_MESSAGE,
                requiresLogin = true
            )
            404 -> ConversationListResult(conversations = emptyList())
            else -> ConversationListResult(
                errorMessage = parseErrorMessage(response) ?: "載入歷史對話失敗，請稍後再試。"
            )
        }
    }

    private suspend fun handleDetailResponse(
        response: Response<ConversationDetailResponse>,
        sessionId: String,
        hasRetriedAfterRecovery: Boolean
    ): ConversationDetailResult {
        if (response.isSuccessful) {
            val body = response.body()
            if (body?.success == true) {
                val resolvedSessionId = body.session_id?.takeIf { it.isNotBlank() } ?: sessionId
                val messages = body.messages.orEmpty().mapNotNull { item ->
                    val role = item.role?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                    val content = item.content?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                    ConversationMessage(
                        role = role,
                        content = content,
                        createdAt = item.created_at
                    )
                }
                return ConversationDetailResult(
                    detail = ConversationDetail(
                        sessionId = resolvedSessionId,
                        title = body.title,
                        createdAt = body.created_at,
                        updatedAt = body.updated_at,
                        messages = messages
                    )
                )
            }
            return ConversationDetailResult(errorMessage = body?.error_message ?: "載入對話內容失敗，請稍後再試。")
        }

        return when (response.code()) {
            401 -> recoverAndRetryDetailIfNeeded(sessionId, hasRetriedAfterRecovery)
            403 -> ConversationDetailResult(
                errorMessage = LOGIN_REQUIRED_MESSAGE,
                requiresLogin = true
            )
            404 -> ConversationDetailResult(errorMessage = "找不到指定的對話。")
            else -> ConversationDetailResult(
                errorMessage = parseErrorMessage(response) ?: "載入對話內容失敗，請稍後再試。"
            )
        }
    }

    private suspend fun handleDeleteResponse(
        response: Response<ConversationDeleteResponse>,
        sessionId: String,
        hasRetriedAfterRecovery: Boolean
    ): ConversationDeleteResult {
        if (response.isSuccessful) {
            val body = response.body()
            if (body?.success == true && body.deleted == true) {
                return ConversationDeleteResult(isDeleted = true)
            }
            return ConversationDeleteResult(errorMessage = body?.error_message ?: "刪除失敗，請稍後再試。")
        }

        return when (response.code()) {
            401 -> recoverAndRetryDeleteIfNeeded(sessionId, hasRetriedAfterRecovery)
            403 -> ConversationDeleteResult(
                errorMessage = LOGIN_REQUIRED_MESSAGE,
                requiresLogin = true
            )
            404 -> ConversationDeleteResult(errorMessage = "找不到指定的對話。")
            else -> ConversationDeleteResult(
                errorMessage = parseErrorMessage(response) ?: "刪除失敗，請稍後再試。"
            )
        }
    }

    private suspend fun recoverAndRetryListIfNeeded(hasRetriedAfterRecovery: Boolean): ConversationListResult {
        if (hasRetriedAfterRecovery) {
            return ConversationListResult(errorMessage = "登入狀態已失效，請重新登入。")
        }
        val recoveryResult = authRepository.refreshRegisteredToken()
        if (recoveryResult.isSuccess) {
            return getConversationsInternal(hasRetriedAfterRecovery = true)
        }
        return ConversationListResult(
            errorMessage = recoveryResult.errorMessage ?: "登入狀態已失效，請重新登入。",
            switchedToGuest = recoveryResult.switchedToGuest,
            requiresLogin = recoveryResult.switchedToGuest
        )
    }

    private suspend fun recoverAndRetryDetailIfNeeded(
        sessionId: String,
        hasRetriedAfterRecovery: Boolean
    ): ConversationDetailResult {
        if (hasRetriedAfterRecovery) {
            return ConversationDetailResult(errorMessage = "登入狀態已失效，請重新登入。")
        }
        val recoveryResult = authRepository.refreshRegisteredToken()
        if (recoveryResult.isSuccess) {
            return getConversationDetailInternal(sessionId = sessionId, hasRetriedAfterRecovery = true)
        }
        return ConversationDetailResult(
            errorMessage = recoveryResult.errorMessage ?: "登入狀態已失效，請重新登入。",
            switchedToGuest = recoveryResult.switchedToGuest,
            requiresLogin = recoveryResult.switchedToGuest
        )
    }

    private suspend fun recoverAndRetryDeleteIfNeeded(
        sessionId: String,
        hasRetriedAfterRecovery: Boolean
    ): ConversationDeleteResult {
        if (hasRetriedAfterRecovery) {
            return ConversationDeleteResult(errorMessage = "登入狀態已失效，請重新登入。")
        }
        val recoveryResult = authRepository.refreshRegisteredToken()
        if (recoveryResult.isSuccess) {
            return deleteConversationInternal(sessionId = sessionId, hasRetriedAfterRecovery = true)
        }
        return ConversationDeleteResult(
            errorMessage = recoveryResult.errorMessage ?: "登入狀態已失效，請重新登入。",
            switchedToGuest = recoveryResult.switchedToGuest,
            requiresLogin = recoveryResult.switchedToGuest
        )
    }

    private fun parseErrorMessage(response: Response<*>): String? {
        val raw = response.errorBody()?.string().orEmpty()
        if (raw.isBlank()) return null
        return runCatching {
            gson.fromJson(raw, ConversationListResponse::class.java)?.error_message
        }.getOrNull()
    }

    companion object {
        private const val TAG = "ConversationRepository"
        private const val LOGIN_REQUIRED_MESSAGE = "登入後可以查看歷史對話。"
    }
}
