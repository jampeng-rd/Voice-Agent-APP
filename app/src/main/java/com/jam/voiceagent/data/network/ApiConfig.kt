package com.jam.voiceagent.data.network

import com.jam.voiceagent.BuildConfig

object ApiConfig {
    val baseUrl: String
        get() = ensureTrailingSlash(BuildConfig.API_BASE_URL)

    fun resolveUrl(pathOrUrl: String): String {
        val trimmed = pathOrUrl.trim()
        if (trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true)
        ) {
            return trimmed
        }

        val base = baseUrl.trimEnd('/')
        val path = trimmed.trimStart('/')
        return "$base/$path"
    }

    private fun ensureTrailingSlash(url: String): String {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) return "http://10.0.2.2:8000/"
        return if (trimmed.endsWith('/')) trimmed else "$trimmed/"
    }
}
