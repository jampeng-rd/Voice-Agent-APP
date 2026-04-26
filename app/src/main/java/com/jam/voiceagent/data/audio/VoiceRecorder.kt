package com.jam.voiceagent.data.audio

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.util.UUID
import kotlin.concurrent.thread

class VoiceRecorder(
    private val cacheDir: File
) {
    private var audioRecord: AudioRecord? = null
    private var recordingThread: Thread? = null
    @Volatile
    private var isRecording = false
    private var currentFile: File? = null
    private var bytesWritten: Long = 0L

    fun startRecording(): Result<File> {
        if (isRecording) return Result.failure(IllegalStateException("錄音已在進行中。"))

        val minBuffer = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT
        )
        if (minBuffer <= 0) return Result.failure(IllegalStateException("無法初始化錄音器。"))

        val outputFile = File(cacheDir, "voice-input-${UUID.randomUUID()}.wav")
        outputFile.parentFile?.mkdirs()

        val recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
            minBuffer * 2
        )
        if (recorder.state != AudioRecord.STATE_INITIALIZED) {
            recorder.release()
            return Result.failure(IllegalStateException("無法初始化錄音器。"))
        }

        return runCatching {
            FileOutputStream(outputFile).use { stream ->
                stream.write(ByteArray(WAV_HEADER_SIZE))
            }

            recorder.startRecording()
            audioRecord = recorder
            currentFile = outputFile
            bytesWritten = 0L
            isRecording = true

            recordingThread = thread(start = true, name = "voice-recorder-thread") {
                val buffer = ByteArray(minBuffer)
                FileOutputStream(outputFile, true).use { stream ->
                    while (isRecording) {
                        val read = recorder.read(buffer, 0, buffer.size)
                        if (read > 0) {
                            stream.write(buffer, 0, read)
                            bytesWritten += read.toLong()
                        }
                    }
                }
            }
            outputFile
        }.onFailure {
            safeStopAndRelease()
            outputFile.delete()
        }
    }

    fun stopRecording(): Result<File> {
        val file = currentFile ?: return Result.failure(IllegalStateException("目前沒有進行中的錄音。"))
        if (!isRecording) return Result.failure(IllegalStateException("目前沒有進行中的錄音。"))

        return runCatching {
            isRecording = false
            recordingThread?.join(1500)

            safeStopAndRelease()
            recordingThread = null

            if (bytesWritten <= 0L) {
                file.delete()
                throw IllegalStateException("錄音內容為空。")
            }

            writeWavHeader(file, bytesWritten)
            file
        }.onFailure {
            file.delete()
            resetState()
        }.onSuccess {
            resetState(keepFile = false)
        }
    }

    fun cancelAndDeleteCurrent() {
        isRecording = false
        try {
            recordingThread?.join(800)
        } catch (_: InterruptedException) {
        }
        safeStopAndRelease()
        recordingThread = null
        currentFile?.delete()
        resetState()
    }

    private fun safeStopAndRelease() {
        val recorder = audioRecord ?: return
        runCatching { recorder.stop() }
        recorder.release()
        audioRecord = null
    }

    private fun resetState(keepFile: Boolean = false) {
        bytesWritten = 0L
        isRecording = false
        if (!keepFile) {
            currentFile = null
        }
    }

    private fun writeWavHeader(file: File, audioLength: Long) {
        val dataLength = audioLength + 36
        val byteRate = SAMPLE_RATE * CHANNEL_COUNT * BITS_PER_SAMPLE / 8

        RandomAccessFile(file, "rw").use { raf ->
            raf.seek(0)
            raf.writeBytes("RIFF")
            raf.writeIntLE(dataLength.toInt())
            raf.writeBytes("WAVE")
            raf.writeBytes("fmt ")
            raf.writeIntLE(16)
            raf.writeShortLE(1)
            raf.writeShortLE(CHANNEL_COUNT.toShort())
            raf.writeIntLE(SAMPLE_RATE)
            raf.writeIntLE(byteRate)
            raf.writeShortLE((CHANNEL_COUNT * BITS_PER_SAMPLE / 8).toShort())
            raf.writeShortLE(BITS_PER_SAMPLE.toShort())
            raf.writeBytes("data")
            raf.writeIntLE(audioLength.toInt())
        }
    }

    private fun RandomAccessFile.writeIntLE(value: Int) {
        write(byteArrayOf(
            (value and 0xff).toByte(),
            ((value shr 8) and 0xff).toByte(),
            ((value shr 16) and 0xff).toByte(),
            ((value shr 24) and 0xff).toByte()
        ))
    }

    private fun RandomAccessFile.writeShortLE(value: Short) {
        write(byteArrayOf(
            (value.toInt() and 0xff).toByte(),
            ((value.toInt() shr 8) and 0xff).toByte()
        ))
    }

    companion object {
        private const val SAMPLE_RATE = 16_000
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val CHANNEL_COUNT = 1
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val BITS_PER_SAMPLE = 16
        private const val WAV_HEADER_SIZE = 44
    }
}
