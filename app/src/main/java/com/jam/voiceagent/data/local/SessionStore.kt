package com.jam.voiceagent.data.local

import android.content.Context
import android.util.Log
import com.jam.voiceagent.data.util.SessionDebug
import java.util.UUID

class SessionStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private var guestSessionId: String? = null

    fun getOrCreateRegisteredSessionId(): String {
        val existing = prefs.getString(KEY_REGISTERED_SESSION_ID, null)
        if (!existing.isNullOrBlank()) return existing

        val newSessionId = UUID.randomUUID().toString()
        prefs.edit().putString(KEY_REGISTERED_SESSION_ID, newSessionId).apply()
        Log.i(TAG, "created new registered session: session=${SessionDebug.short(newSessionId)}")
        return newSessionId
    }

    fun getOrCreateGuestSessionId(): String {
        val existing = guestSessionId
        if (!existing.isNullOrBlank()) return existing

        val newSessionId = UUID.randomUUID().toString()
        guestSessionId = newSessionId
        return newSessionId
    }

    fun getOrCreateSessionId(isRegistered: Boolean): String {
        return if (isRegistered) {
            getOrCreateRegisteredSessionId()
        } else {
            getOrCreateGuestSessionId()
        }
    }

    fun promoteGuestSessionToRegistered() {
        val guestSession = guestSessionId
        if (guestSession.isNullOrBlank()) return
        prefs.edit().putString(KEY_REGISTERED_SESSION_ID, guestSession).apply()
        guestSessionId = null
    }

    fun clearRegisteredSessionId() {
        val previous = peekRegisteredSessionId()
        Log.i(TAG, "clear registered session: previous=${SessionDebug.short(previous)}")
        prefs.edit().remove(KEY_REGISTERED_SESSION_ID).apply()
    }

    fun clearGuestSessionId() {
        guestSessionId = null
    }

    fun clearAllSessionIds() {
        clearRegisteredSessionId()
        clearGuestSessionId()
    }

    fun hasGuestSessionId(): Boolean = !guestSessionId.isNullOrBlank()

    fun peekGuestSessionId(): String? = guestSessionId

    fun peekRegisteredSessionId(): String? = prefs.getString(KEY_REGISTERED_SESSION_ID, null)

    fun saveRegisteredSessionId(sessionId: String) {
        Log.i(TAG, "save registered session: session=${SessionDebug.short(sessionId)}")
        prefs.edit().putString(KEY_REGISTERED_SESSION_ID, sessionId).apply()
    }

    fun replaceRegisteredWithGuestSessionIfAvailable() {
        val guestSession = guestSessionId
        if (guestSession.isNullOrBlank()) return
        saveRegisteredSessionId(guestSession)
        guestSessionId = null
    }

    fun createFreshGuestSessionId(): String {
        val newSessionId = UUID.randomUUID().toString()
        guestSessionId = newSessionId
        return newSessionId
    }

    companion object {
        private const val TAG = "SessionStore"
        private const val PREFS_NAME = "voice_agent_prefs"
        private const val KEY_REGISTERED_SESSION_ID = "registered_chat_session_id"
    }
}
