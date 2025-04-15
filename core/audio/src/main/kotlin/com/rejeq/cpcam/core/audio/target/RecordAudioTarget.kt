package com.rejeq.cpcam.core.audio.target

import com.rejeq.cpcam.core.audio.source.AudioSource
import java.nio.ByteBuffer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordAudioTarget @Inject constructor(private val source: AudioSource) {
    fun start() = source.start()

    fun stop() = source.stop()

    fun read(buffer: ByteBuffer) = source.read(buffer)

    fun isRecording() = source.isRecording
}
