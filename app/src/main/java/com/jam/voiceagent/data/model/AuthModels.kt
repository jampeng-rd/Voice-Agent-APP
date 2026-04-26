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
    val refresh_token: String?,
    val user_id: Long?,
    val email: String?,
    val expires_at: String?,
    val refresh_expires_at: String?,
    val error_message: String?
)

data class RefreshTokenRequest(
    val refresh_token: String
)

data class RefreshTokenResponse(
    val success: Boolean,
    val token: String?,
    val refresh_token: String?,
    val expires_at: String?,
    val refresh_expires_at: String?,
    val user_id: Long?,
    val email: String?,
    val error_message: String?
)

data class GuestAuthResponse(
    val success: Boolean,
    val token: String?,
    val guest_id: String?,
    val expires_at: String?,
    val error_message: String?
)
