package com.jam.voiceagent.data.model

data class VoiceRoundResponse(
    val success: Boolean,
    val session_id: String,
    val identity_type: String,
    val user_id: Long?,
    val guest_id: String?,
    val user_text: String,
    val ai_reply: String,
    val input_wav: String,
    val output_wav: String,
    val error_message: String?
)
