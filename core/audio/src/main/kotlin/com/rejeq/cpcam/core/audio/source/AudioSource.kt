package com.rejeq.cpcam.core.audio.source

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.nio.ByteBuffer
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@Singleton
class AudioSource @Inject constructor(
    @ApplicationContext val context: Context,
) {
    private var config: AudioStreamConfig = AudioStreamConfig()
    private var audioStream: AudioStream? = null

    var isRecording = false
        private set

    fun isConfigSupported(config: AudioStreamConfig): Boolean =
        AudioStream.isConfigSupported(config)

    fun updateConfig(newConfig: AudioStreamConfig): Result<Unit> =
        synchronized(this) {
            if (isRecording) {
                stop()
            }
            config = newConfig
            return Result.success(Unit)
        }

    @OptIn(ExperimentalAtomicApi::class)
    fun start(): Result<Unit> = synchronized(this) {
        if (isRecording) {
            return Result.success(Unit)
        }

        return runCatching {
            audioStream = AudioStream(config, context)
            audioStream?.initialize()?.getOrThrow()
            audioStream?.start()?.getOrThrow()

            isRecording = true
            Result.success(Unit)
        }.getOrElse { e ->
            audioStream = null
            Result.failure(
                RuntimeException("Failed to start audio source: ${e.message}"),
            )
        }
    }

    fun read(buffer: ByteBuffer): AudioData = synchronized(this) {
        val audioStream = audioStream
        requireNotNull(audioStream)

        audioStream.read(buffer)
    }

    fun stop() = synchronized(this) {
        if (!isRecording) {
            return@synchronized
        }

        audioStream?.stop()
        audioStream = null
        isRecording = false
        Result.success(Unit)
    }
}
