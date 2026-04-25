package com.jam.voiceagent.data.network

import com.jam.voiceagent.BuildConfig

object ApiConfig {
    val baseUrl: String
        get() = ensureTrailingSlash(BuildConfig.API_BASE_URL)

    private fun ensureTrailingSlash(url: String): String {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) return "http://10.0.2.2:8000/"
        return if (trimmed.endsWith('/')) trimmed else "$trimmed/"
    }
}
