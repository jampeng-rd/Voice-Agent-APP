package com.jam.voiceagent.data.network

import com.jam.voiceagent.data.model.ConversationDeleteResponse
import com.jam.voiceagent.data.model.ConversationDetailResponse
import com.jam.voiceagent.data.model.ConversationListResponse
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface ConversationApi {
    @GET("api/conversations")
    suspend fun getConversations(
        @Header("Authorization") authorization: String
    ): Response<ConversationListResponse>

    @GET("api/conversations/{session_id}")
    suspend fun getConversationDetail(
        @Header("Authorization") authorization: String,
        @Path("session_id") sessionId: String
    ): Response<ConversationDetailResponse>

    @DELETE("api/conversations/{session_id}")
    suspend fun deleteConversation(
        @Header("Authorization") authorization: String,
        @Path("session_id") sessionId: String
    ): Response<ConversationDeleteResponse>
}
