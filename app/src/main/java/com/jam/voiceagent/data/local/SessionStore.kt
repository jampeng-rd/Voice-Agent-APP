package com.jam.voiceagent.data.local

import android.content.Context
import java.util.UUID

class SessionStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getOrCreateSessionId(): String {
        val existing = prefs.getString(KEY_SESSION_ID, null)
        if (!existing.isNullOrBlank()) return existing

        val newSessionId = UUID.randomUUID().toString()
        prefs.edit().putString(KEY_SESSION_ID, newSessionId).apply()
        return newSessionId
    }

    fun clearSessionId() {
        prefs.edit().remove(KEY_SESSION_ID).apply()
    }

    companion object {
        private const val PREFS_NAME = "voice_agent_prefs"
        private const val KEY_SESSION_ID = "chat_session_id"
    }
}
