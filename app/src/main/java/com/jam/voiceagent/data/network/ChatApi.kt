package com.jam.voiceagent.data.network

import com.jam.voiceagent.data.model.ChatRequest
import com.jam.voiceagent.data.model.ChatResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ChatApi {
    @POST("api/chat")
    suspend fun sendText(
        @Header("Authorization") authorization: String,
        @Body request: ChatRequest
    ): Response<ChatResponse>
}
