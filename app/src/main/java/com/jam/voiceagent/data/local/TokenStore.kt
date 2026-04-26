package com.jam.voiceagent.data.local

import android.content.Context

class TokenStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private var guestToken: String? = null
    private var guestId: String? = null
    private var guestExpiresAt: String? = null

    fun saveRegisteredAuth(
        accessToken: String,
        expiresAt: String?,
        refreshToken: String?,
        refreshExpiresAt: String?
    ) {
        prefs.edit()
            .putString(KEY_REGISTERED_TOKEN, accessToken)
            .putString(KEY_REGISTERED_EXPIRES_AT, expiresAt)
            .putString(KEY_REGISTERED_REFRESH_TOKEN, refreshToken)
            .putString(KEY_REGISTERED_REFRESH_EXPIRES_AT, refreshExpiresAt)
            .apply()
    }

    fun getRegisteredToken(): String? = prefs.getString(KEY_REGISTERED_TOKEN, null)
    fun getRegisteredExpiresAt(): String? = prefs.getString(KEY_REGISTERED_EXPIRES_AT, null)
    fun getRegisteredRefreshToken(): String? = prefs.getString(KEY_REGISTERED_REFRESH_TOKEN, null)
    fun getRegisteredRefreshExpiresAt(): String? = prefs.getString(KEY_REGISTERED_REFRESH_EXPIRES_AT, null)

    fun hasRegisteredToken(): Boolean = !getRegisteredToken().isNullOrBlank()

    fun saveGuestAuth(token: String, guestId: String?, expiresAt: String?) {
        guestToken = token
        this.guestId = guestId
        guestExpiresAt = expiresAt
    }

    fun getGuestToken(): String? = guestToken

    fun hasGuestToken(): Boolean = !guestToken.isNullOrBlank()

    fun clearGuestAuth() {
        guestToken = null
        guestId = null
        guestExpiresAt = null
    }

    fun clearRegisteredAuth() {
        prefs.edit()
            .remove(KEY_REGISTERED_TOKEN)
            .remove(KEY_REGISTERED_EXPIRES_AT)
            .remove(KEY_REGISTERED_REFRESH_TOKEN)
            .remove(KEY_REGISTERED_REFRESH_EXPIRES_AT)
            .apply()
    }

    fun getGuestId(): String? = guestId

    fun getGuestExpiresAt(): String? = guestExpiresAt

    fun getActiveToken(): String? = getRegisteredToken() ?: guestToken

    fun hasAnyToken(): Boolean = !getActiveToken().isNullOrBlank()

    fun isUsingRegisteredToken(): Boolean = hasRegisteredToken()

    fun isUsingGuestToken(): Boolean = !hasRegisteredToken() && hasGuestToken()

    fun clearAllAuth() {
        clearRegisteredAuth()
        clearGuestAuth()
    }

    companion object {
        private const val PREFS_NAME = "voice_agent_prefs"
        private const val KEY_REGISTERED_TOKEN = "registered_auth_token"
        private const val KEY_REGISTERED_EXPIRES_AT = "registered_auth_expires_at"
        private const val KEY_REGISTERED_REFRESH_TOKEN = "registered_refresh_token"
        private const val KEY_REGISTERED_REFRESH_EXPIRES_AT = "registered_refresh_expires_at"
    }
}
