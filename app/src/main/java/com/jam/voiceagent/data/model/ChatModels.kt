package com.jam.voiceagent.data.model

data class ChatRequest(
    val session_id: String,
    val text: String
)

data class ChatResponse(
    val success: Boolean,
    val session_id: String?,
    val identity_type: String?,
    val user_id: Long?,
    val guest_id: String?,
    val user_text: String?,
    val ai_reply: String?,
    val error_message: String?
)
