package com.jam.voiceagent.data.audio

import android.media.AudioAttributes
import android.media.MediaPlayer
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

class VoicePlayer {
    private var mediaPlayer: MediaPlayer? = null

    suspend fun playAndAwait(file: File) {
        stopAndRelease()
        return suspendCancellableCoroutine { continuation ->
            val player = MediaPlayer()
            mediaPlayer = player

            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )

            player.setOnPreparedListener {
                runCatching { it.start() }.onFailure { error ->
                    stopAndRelease()
                    if (continuation.isActive) {
                        continuation.resumeWithException(error)
                    }
                }
            }

            player.setOnCompletionListener {
                stopAndRelease()
                if (continuation.isActive) {
                    continuation.resume(Unit)
                }
            }

            player.setOnErrorListener { _, _, _ ->
                stopAndRelease()
                if (continuation.isActive) {
                    continuation.resumeWithException(IllegalStateException("播放回覆時發生問題，請稍後再試。"))
                }
                true
            }

            runCatching {
                player.setDataSource(file.absolutePath)
                player.prepareAsync()
            }.onFailure { error ->
                stopAndRelease()
                if (continuation.isActive) {
                    continuation.resumeWithException(error)
                }
            }

            continuation.invokeOnCancellation {
                stopAndRelease()
            }
        }
    }

    fun stopAndRelease() {
        mediaPlayer?.let { player ->
            runCatching {
                if (player.isPlaying) player.stop()
            }
            player.release()
        }
        mediaPlayer = null
    }
}
