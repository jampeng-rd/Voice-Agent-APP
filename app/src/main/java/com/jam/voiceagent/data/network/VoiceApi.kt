package com.jam.voiceagent.data.network

import com.jam.voiceagent.data.model.VoiceRoundResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface VoiceApi {
    @Multipart
    @POST("api/voice/round")
    suspend fun round(
        @Header("Authorization") authorization: String,
        @Part audio_file: MultipartBody.Part,
        @Part("session_id") sessionId: RequestBody
    ): Response<VoiceRoundResponse>
}
