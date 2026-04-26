package com.jam.voiceagent.data.local

import android.content.Context

class TokenStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private var guestToken: String? = null
    private var guestId: String? = null
    private var guestExpiresAt: String? = null

    fun saveRegisteredToken(token: String) {
        prefs.edit().putString(KEY_REGISTERED_TOKEN, token).apply()
    }

    fun getRegisteredToken(): String? = prefs.getString(KEY_REGISTERED_TOKEN, null)

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

    fun clearRegisteredToken() {
        prefs.edit().remove(KEY_REGISTERED_TOKEN).apply()
    }

    fun getGuestId(): String? = guestId

    fun getGuestExpiresAt(): String? = guestExpiresAt

    fun getActiveToken(): String? = getRegisteredToken() ?: guestToken

    fun hasAnyToken(): Boolean = !getActiveToken().isNullOrBlank()

    fun isUsingRegisteredToken(): Boolean = hasRegisteredToken()

    fun isUsingGuestToken(): Boolean = !hasRegisteredToken() && hasGuestToken()

    fun clearAllAuth() {
        clearRegisteredToken()
        clearGuestAuth()
    }

    companion object {
        private const val PREFS_NAME = "voice_agent_prefs"
        private const val KEY_REGISTERED_TOKEN = "registered_auth_token"
    }
}
