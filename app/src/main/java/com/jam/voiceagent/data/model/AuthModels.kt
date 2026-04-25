package com.jam.voiceagent.data.model

data class AuthRequest(
    val email: String,
    val password: String
)

data class RegisterResponse(
    val success: Boolean,
    val user_id: Long?,
    val email: String?,
    val error_message: String?
)

data class LoginResponse(
    val success: Boolean,
    val token: String?,
    val user_id: Long?,
    val email: String?,
    val expires_at: String?,
    val error_message: String?
)
