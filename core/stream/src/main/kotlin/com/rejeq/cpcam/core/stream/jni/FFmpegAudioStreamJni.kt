package com.rejeq.cpcam.core.stream.jni

import java.nio.ByteBuffer

class FFmpegAudioStreamJni(val handle: Long) {
    companion object {
        init {
            System.loadLibrary("cpcam_jni")
        }
    }

    fun send(
        ts: Long,
        sampleCount: Int,
        sampleRate: Int,
        channelCount: Int,
        format: FFmpegSampleFormat,
        buffer: ByteBuffer,
    ) = send(
        handle,
        ts,
        sampleCount,
        sampleRate,
        channelCount,
        format.ordinal,
        buffer,
    )

    fun start() = start(handle)

    fun stop() = stop(handle)

    private external fun send(
        handle: Long,
        ts: Long,
        sampleCount: Int,
        sampleRate: Int,
        channelCount: Int,
        format: Int,
        buffers: ByteBuffer,
    )

    private external fun start(handle: Long)
    private external fun stop(handle: Long)
}
