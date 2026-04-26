package com.jam.voiceagent.data.repository

import android.util.Log
import com.google.gson.Gson
import com.jam.voiceagent.data.local.SessionStore
import com.jam.voiceagent.data.local.TokenStore
import com.jam.voiceagent.data.model.AuthRequest
import com.jam.voiceagent.data.model.GuestAuthResponse
import com.jam.voiceagent.data.model.LoginResponse
import com.jam.voiceagent.data.model.RefreshTokenRequest
import com.jam.voiceagent.data.model.RefreshTokenResponse
import com.jam.voiceagent.data.model.RegisterResponse
import com.jam.voiceagent.data.network.AuthApi
import retrofit2.Response

data class TokenRecoveryResult(
    val isSuccess: Boolean,
    val switchedToGuest: Boolean = false,
    val errorMessage: String? = null
)

class AuthRepository(
    private val authApi: AuthApi,
    private val tokenStore: TokenStore,
    private val sessionStore: SessionStore
) {
    private val gson = Gson()

    suspend fun register(email: String, password: String): RepositoryResult<Unit> {
        return runCatching {
            authApi.register(AuthRequest(email = email, password = password))
        }.fold(
            onSuccess = { response ->
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        RepositoryResult(data = Unit)
                    } else {
                        RepositoryResult(errorMessage = body?.error_message ?: "註冊失敗，請稍後再試。")
                    }
                } else {
                    RepositoryResult(errorMessage = parseRegisterError(response) ?: "註冊失敗，請稍後再試。")
                }
            },
            onFailure = {
                RepositoryResult(errorMessage = "目前連線有點問題，請稍後再試。")
            }
        )
    }

    suspend fun login(email: String, password: String): RepositoryResult<Unit> {
        return runCatching {
            authApi.login(AuthRequest(email = email, password = password))
        }.fold(
            onSuccess = { response ->
                if (response.isSuccessful) {
                    val body = response.body()
                    val token = body?.token
                    if (body?.success == true && !token.isNullOrBlank()) {
                        tokenStore.saveRegisteredAuth(
                            accessToken = token,
                            expiresAt = body.expires_at,
                            refreshToken = body.refresh_token,
                            refreshExpiresAt = body.refresh_expires_at
                        )
                        sessionStore.promoteGuestSessionToRegistered()
                        tokenStore.clearGuestAuth()
                        RepositoryResult(data = Unit)
                    } else {
                        RepositoryResult(errorMessage = body?.error_message ?: "登入失敗，請確認帳號密碼。")
                    }
                } else {
                    RepositoryResult(errorMessage = parseLoginError(response) ?: "登入失敗，請確認帳號密碼。")
                }
            },
            onFailure = {
                RepositoryResult(errorMessage = "目前連線有點問題，請稍後再試。")
            }
        )
    }

    suspend fun ensureGuestToken(): RepositoryResult<Unit> {
        if (tokenStore.hasRegisteredToken() || tokenStore.hasGuestToken()) {
            return RepositoryResult(data = Unit)
        }
        return createGuestToken()
    }

    suspend fun createGuestToken(): RepositoryResult<Unit> {
        return runCatching {
            authApi.guest()
        }.fold(
            onSuccess = { response ->
                if (response.isSuccessful) {
                    val body = response.body()
                    val token = body?.token
                    if (body?.success == true && !token.isNullOrBlank()) {
                        tokenStore.saveGuestAuth(
                            token = token,
                            guestId = body.guest_id,
                            expiresAt = body.expires_at
                        )
                        RepositoryResult(data = Unit)
                    } else {
                        RepositoryResult(errorMessage = body?.error_message ?: "目前無法建立訪客連線，請稍後再試。")
                    }
                } else {
                    RepositoryResult(errorMessage = parseGuestError(response) ?: "目前無法建立訪客連線，請稍後再試。")
                }
            },
            onFailure = {
                RepositoryResult(errorMessage = "目前無法建立訪客連線，請稍後再試。")
            }
        )
    }

    suspend fun recreateGuestTokenIfNeeded(): TokenRecoveryResult {
        tokenStore.clearGuestAuth()
        val guestResult = createGuestToken()
        if (guestResult.isSuccess) {
            Log.i(TAG, "token recovery: recreated guest token")
            return TokenRecoveryResult(isSuccess = true)
        }
        Log.w(TAG, "token recovery: recreate guest failed")
        return TokenRecoveryResult(
            isSuccess = false,
            errorMessage = guestResult.errorMessage ?: "目前無法建立訪客連線，請稍後再試。"
        )
    }

    suspend fun refreshRegisteredToken(): TokenRecoveryResult {
        val refreshToken = tokenStore.getRegisteredRefreshToken()
        if (refreshToken.isNullOrBlank()) {
            Log.w(TAG, "token recovery: missing refresh token")
            return handleRefreshFailureAndFallbackToGuest()
        }

        val refreshResponse = runCatching {
            authApi.refresh(RefreshTokenRequest(refresh_token = refreshToken))
        }.getOrElse {
            Log.w(TAG, "token recovery: refresh request failed")
            return TokenRecoveryResult(
                isSuccess = false,
                errorMessage = "目前連線有點問題，請稍後再試。"
            )
        }

        if (!refreshResponse.isSuccessful) {
            val code = refreshResponse.code()
            Log.w(TAG, "token recovery: refresh response failed code=$code")
            if (code == 401) {
                return handleRefreshFailureAndFallbackToGuest()
            }
            return TokenRecoveryResult(
                isSuccess = false,
                errorMessage = parseRefreshError(refreshResponse) ?: "目前連線有點問題，請稍後再試。"
            )
        }

        val body = refreshResponse.body()
        val accessToken = body?.token
        if (body?.success == true && !accessToken.isNullOrBlank()) {
            tokenStore.saveRegisteredAuth(
                accessToken = accessToken,
                expiresAt = body.expires_at,
                refreshToken = body.refresh_token,
                refreshExpiresAt = body.refresh_expires_at
            )
            Log.i(TAG, "token recovery: refresh success")
            return TokenRecoveryResult(isSuccess = true)
        }

        Log.w(TAG, "token recovery: refresh body invalid")
        return handleRefreshFailureAndFallbackToGuest()
    }

    fun hasRegisteredToken(): Boolean = tokenStore.hasRegisteredToken()

    fun clearGuestAuthMemoryOnly() {
        tokenStore.clearGuestAuth()
        sessionStore.clearGuestSessionId()
    }

    suspend fun logoutRegisteredAndCreateGuest(): RepositoryResult<Unit> {
        tokenStore.clearRegisteredAuth()
        sessionStore.clearRegisteredSessionId()
        clearGuestAuthMemoryOnly()
        return createGuestToken()
    }

    private suspend fun handleRefreshFailureAndFallbackToGuest(): TokenRecoveryResult {
        tokenStore.clearRegisteredAuth()
        sessionStore.clearRegisteredSessionId()
        val guestResult = recreateGuestTokenIfNeeded()
        if (!guestResult.isSuccess) {
            return guestResult
        }
        return TokenRecoveryResult(
            isSuccess = false,
            switchedToGuest = true,
            errorMessage = "登入已過期，已切換為訪客模式。"
        )
    }

    private fun parseRegisterError(response: Response<RegisterResponse>): String? {
        val raw = response.errorBody()?.string().orEmpty()
        if (raw.isBlank()) return null
        return runCatching { gson.fromJson(raw, RegisterResponse::class.java)?.error_message }.getOrNull()
    }

    private fun parseLoginError(response: Response<LoginResponse>): String? {
        val raw = response.errorBody()?.string().orEmpty()
        if (raw.isBlank()) return null
        return runCatching { gson.fromJson(raw, LoginResponse::class.java)?.error_message }.getOrNull()
    }

    private fun parseGuestError(response: Response<GuestAuthResponse>): String? {
        val raw = response.errorBody()?.string().orEmpty()
        if (raw.isBlank()) return null
        return runCatching { gson.fromJson(raw, GuestAuthResponse::class.java)?.error_message }.getOrNull()
    }

    private fun parseRefreshError(response: Response<RefreshTokenResponse>): String? {
        val raw = response.errorBody()?.string().orEmpty()
        if (raw.isBlank()) return null
        return runCatching { gson.fromJson(raw, RefreshTokenResponse::class.java)?.error_message }.getOrNull()
    }

    companion object {
        private const val TAG = "AuthRepository"
    }
}
