package com.rejeq.cpcam.core.audio.source

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.MediaRecorder
import androidx.camera.video.internal.audio.AudioSettings
import androidx.camera.video.internal.audio.AudioStreamImpl
import java.nio.ByteBuffer

sealed interface AudioStreamError {
    data object NotInitialized : AudioStreamError
    data object StartFailed : AudioStreamError
    data object ReadFailed : AudioStreamError
    data class UnknownError(val message: String) : AudioStreamError
}

data class AudioStreamConfig(
    val source: Int = MediaRecorder.AudioSource.MIC,
    val sampleRate: Int = 44100,
    val channelCount: Int = 2,
    val audioFormat: Int = AudioFormat.ENCODING_PCM_FLOAT,
)

data class AudioData(val sizeInBytes: Int, val timestampNs: Long)

@SuppressLint("RestrictedApi")
class AudioStream(
    private val config: AudioStreamConfig,
    private val context: Context,
) {
    private var stream: AudioStreamImpl? = null

    fun initialize(): Result<Unit> = runCatching {
        stream = AudioStreamImpl(
            AudioSettings.builder().apply {
                setAudioSource(config.source)
                setSampleRate(44100)
                setChannelCount(1)
                setAudioFormat(AudioFormat.ENCODING_PCM_FLOAT)

//                setSampleRate(config.sampleRate)
//                setChannelCount(config.channelCount)
//                setAudioFormat(config.audioFormat)
            }.build(),
            context,
        )
        Result.success(Unit)
    }.getOrElse { e ->
        Result.failure(
            RuntimeException("Failed to initialize AudioStream: ${e.message}"),
        )
    }

    fun start(): Result<Unit> = when {
        stream == null -> Result.failure(
            RuntimeException(AudioStreamError.NotInitialized.toString()),
        )
        else -> runCatching {
            stream?.start()
            Result.success(Unit)
        }.getOrElse { e ->
            Result.failure(
                RuntimeException("Failed to start AudioStream: ${e.message}"),
            )
        }
    }

    fun read(buffer: ByteBuffer): AudioData {
        val stream = stream
        requireNotNull(stream) { "Stream must be started at this point" }

        val packetInfo = stream.read(buffer)

        // TODO: Optimization, since sizeInBytes is always const, you can return
        //  only timestamp
        return AudioData(
            sizeInBytes = packetInfo.sizeInBytes,
            timestampNs = packetInfo.timestampNs,
        )
    }

    fun stop() {
        // FIXME: you should call AudioRecord.release() when object is destroyed
        stream?.stop()
        stream = null
    }

    companion object {
        @SuppressLint("RestrictedApi")
        fun isConfigSupported(config: AudioStreamConfig) =
            AudioStreamImpl.isSettingsSupported(
                config.sampleRate,
                config.channelCount,
                config.audioFormat,
            )
    }
}
