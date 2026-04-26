package com.jam.voiceagent.data.repository

import android.util.Log
import com.google.gson.Gson
import com.jam.voiceagent.data.local.SessionStore
import com.jam.voiceagent.data.local.TokenStore
import com.jam.voiceagent.data.model.VoiceRoundResponse
import com.jam.voiceagent.data.network.ApiClient
import com.jam.voiceagent.data.network.ApiConfig
import com.jam.voiceagent.data.network.VoiceApi
import java.io.File
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

data class VoiceRoundResult(
    val aiReply: String? = null,
    val replyAudioFile: File? = null,
    val errorMessage: String? = null
) {
    val isSuccess: Boolean = errorMessage == null
}

class VoiceRepository(
    private val voiceApi: VoiceApi,
    private val tokenStore: TokenStore,
    private val sessionStore: SessionStore,
    private val cacheDir: File
) {
    private val gson = Gson()
    private data class ResolvedAudio(
        val file: File? = null,
        val errorMessage: String? = null
    )

    suspend fun sendVoiceRound(recordedWavFile: File): VoiceRoundResult {
        val usingRegistered = tokenStore.isUsingRegisteredToken()
        val token = if (usingRegistered) tokenStore.getRegisteredToken() else tokenStore.getGuestToken()
        if (token.isNullOrBlank()) {
            return VoiceRoundResult(errorMessage = "目前無法建立訪客連線，請稍後再試。")
        }

        if (!recordedWavFile.exists() || recordedWavFile.length() <= 0L) {
            return VoiceRoundResult(errorMessage = "錄音時發生問題，請再試一次。")
        }

        val sessionId = sessionStore.getOrCreateSessionId(isRegistered = usingRegistered)
        val audioPart = MultipartBody.Part.createFormData(
            "audio_file",
            recordedWavFile.name,
            recordedWavFile.asRequestBody("audio/wav".toMediaType())
        )
        val sessionPart = sessionId.toRequestBody("text/plain".toMediaType())

        return runCatching {
            voiceApi.round(
                authorization = "Bearer $token",
                audio_file = audioPart,
                sessionId = sessionPart
            )
        }.fold(
            onSuccess = { response ->
                handleVoiceResponse(response, sessionId)
            },
            onFailure = { throwable ->
                val uiMessage = classifyThrowable(throwable)
                Log.w(TAG, "voice round failed: sessionId=$sessionId type=$uiMessage")
                VoiceRoundResult(errorMessage = uiMessage)
            }
        )
    }

    fun deleteTempAudio(file: File?) {
        if (file == null) return
        runCatching {
            if (file.exists()) file.delete()
        }
    }

    private suspend fun handleVoiceResponse(
        response: Response<VoiceRoundResponse>,
        sessionId: String
    ): VoiceRoundResult {
        if (!response.isSuccessful) {
            val parsed = parseErrorMessage(response)
            val code = response.code()
            val message = when {
                code == 401 && tokenStore.isUsingRegisteredToken() -> parsed ?: "登入狀態已失效，請重新登入。"
                code in 400..499 -> parsed ?: "語音請求格式或內容有誤，請稍後再試。"
                code >= 500 -> parsed ?: "伺服器忙碌中，請稍後再試。"
                else -> parsed ?: "目前連線有點問題，請稍後再試。"
            }
            Log.w(TAG, "voice round response failed: code=$code sessionId=$sessionId")
            return VoiceRoundResult(errorMessage = message)
        }

        val body = response.body()
        if (body == null) {
            Log.w(TAG, "voice round empty body: sessionId=$sessionId")
            return VoiceRoundResult(errorMessage = "回覆解析失敗，請稍後再試。")
        }

        if (!body.success) {
            return VoiceRoundResult(errorMessage = body.error_message ?: "語音回合處理失敗，請稍後再試。")
        }

        val resolvedAudio = resolveReplyAudioFile(
            outputAudioUrl = body.output_audio_url,
            outputWav = body.output_wav
        )
        if (!resolvedAudio.errorMessage.isNullOrBlank()) {
            return VoiceRoundResult(
                aiReply = body.ai_reply?.takeIf { it.isNotBlank() },
                errorMessage = resolvedAudio.errorMessage
            )
        }

        return VoiceRoundResult(
            aiReply = body.ai_reply?.takeIf { it.isNotBlank() },
            replyAudioFile = resolvedAudio.file
        )
    }

    private suspend fun resolveReplyAudioFile(
        outputAudioUrl: String?,
        outputWav: String?
    ): ResolvedAudio {
        val downloadCandidate = outputAudioUrl?.trim().takeUnless { it.isNullOrBlank() }
            ?: outputWav?.trim().takeUnless { it.isNullOrBlank() }
            ?: return ResolvedAudio()

        val downloadUrl = toDownloadUrl(downloadCandidate) ?: return ResolvedAudio()
        return downloadAudioToCache(downloadUrl)
    }

    private fun toDownloadUrl(rawValue: String): String? {
        val trimmed = rawValue.trim()
        if (trimmed.isBlank()) return null

        if (trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true) ||
            trimmed.startsWith("/api/", ignoreCase = true) ||
            trimmed.startsWith("api/", ignoreCase = true)
        ) {
            return ApiConfig.resolveUrl(trimmed)
        }

        Log.w(TAG, "skip non-downloadable audio path")
        return null
    }

    private suspend fun downloadAudioToCache(url: String): ResolvedAudio = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(url).get().build()
        val response = runCatching { ApiClient.rawHttpClient.newCall(request).execute() }
            .getOrElse { throwable ->
                val message = when (throwable) {
                    is SocketTimeoutException -> "語音回覆下載逾時，請稍後再試。"
                    is UnknownHostException,
                    is ConnectException -> "目前無法連線到伺服器，請確認網路與伺服器狀態。"
                    is IOException -> "語音回覆下載失敗，請稍後再試。"
                    else -> "語音回覆下載失敗，請稍後再試。"
                }
                return@withContext ResolvedAudio(errorMessage = message)
            }

        response.use { rawResponse ->
            if (!rawResponse.isSuccessful) {
                return@withContext ResolvedAudio(
                    errorMessage = when (rawResponse.code) {
                        404 -> "語音回覆已過期，請再試一次。"
                        else -> "語音回覆下載失敗，請稍後再試。"
                    }
                )
            }

            val body = rawResponse.body ?: return@withContext ResolvedAudio(
                errorMessage = "語音回覆下載失敗，請稍後再試。"
            )

            val target = File(cacheDir, "voice-reply-${UUID.randomUUID()}.wav")
            return@withContext runCatching {
                target.outputStream().use { output ->
                    body.byteStream().use { input ->
                        input.copyTo(output)
                    }
                }
                ResolvedAudio(file = target)
            }.getOrElse {
                runCatching { if (target.exists()) target.delete() }
                ResolvedAudio(errorMessage = "語音回覆下載失敗，請稍後再試。")
            }
        }
    }

    private fun parseErrorMessage(response: Response<VoiceRoundResponse>): String? {
        val raw = response.errorBody()?.string().orEmpty()
        if (raw.isBlank()) return null
        return runCatching { gson.fromJson(raw, VoiceRoundResponse::class.java)?.error_message }.getOrNull()
    }

    private fun classifyThrowable(throwable: Throwable): String {
        return when (throwable) {
            is SocketTimeoutException -> "伺服器回應較慢，請稍後再試。"
            is UnknownHostException,
            is ConnectException -> "目前無法連線到伺服器，請確認網路與伺服器狀態。"
            is IOException -> "目前連線有點問題，請稍後再試。"
            else -> "目前連線有點問題，請稍後再試。"
        }
    }

    companion object {
        private const val TAG = "VoiceRepository"
    }
}
