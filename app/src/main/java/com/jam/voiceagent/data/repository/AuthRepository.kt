package com.jam.voiceagent.data.repository

import com.google.gson.Gson
import com.jam.voiceagent.data.local.TokenStore
import com.jam.voiceagent.data.model.AuthRequest
import com.jam.voiceagent.data.model.LoginResponse
import com.jam.voiceagent.data.model.RegisterResponse
import com.jam.voiceagent.data.network.AuthApi
import retrofit2.Response

class AuthRepository(
    private val authApi: AuthApi,
    private val tokenStore: TokenStore
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
                        tokenStore.saveToken(token)
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

    fun hasToken(): Boolean = !tokenStore.getToken().isNullOrBlank()

    fun clearToken() {
        tokenStore.clearToken()
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
}
