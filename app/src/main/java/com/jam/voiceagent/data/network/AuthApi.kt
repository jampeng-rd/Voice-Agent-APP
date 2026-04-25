package com.jam.voiceagent.data.network

import com.jam.voiceagent.data.model.AuthRequest
import com.jam.voiceagent.data.model.LoginResponse
import com.jam.voiceagent.data.model.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("api/auth/register")
    suspend fun register(
        @Body request: AuthRequest
    ): Response<RegisterResponse>

    @POST("api/auth/login")
    suspend fun login(
        @Body request: AuthRequest
    ): Response<LoginResponse>
}
