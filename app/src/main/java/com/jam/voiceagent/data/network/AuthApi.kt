package com.jam.voiceagent.data.network

import com.jam.voiceagent.data.model.AuthRequest
import com.jam.voiceagent.data.model.GuestAuthResponse
import com.jam.voiceagent.data.model.LoginResponse
import com.jam.voiceagent.data.model.RefreshTokenRequest
import com.jam.voiceagent.data.model.RefreshTokenResponse
import com.jam.voiceagent.data.model.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("api/auth/guest")
    suspend fun guest(): Response<GuestAuthResponse>

    @POST("api/auth/register")
    suspend fun register(
        @Body request: AuthRequest
    ): Response<RegisterResponse>

    @POST("api/auth/login")
    suspend fun login(
        @Body request: AuthRequest
    ): Response<LoginResponse>

    @POST("api/auth/refresh")
    suspend fun refresh(
        @Body request: RefreshTokenRequest
    ): Response<RefreshTokenResponse>
}
