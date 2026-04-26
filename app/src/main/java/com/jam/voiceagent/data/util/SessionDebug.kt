package com.jam.voiceagent.data.util

object SessionDebug {
    fun short(sessionId: String?): String {
        if (sessionId.isNullOrBlank()) return "none"
        return "..." + sessionId.takeLast(6)
    }
}
