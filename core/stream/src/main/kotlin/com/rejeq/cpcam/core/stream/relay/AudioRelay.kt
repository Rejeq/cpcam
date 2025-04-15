package com.rejeq.cpcam.core.stream.relay

import com.rejeq.cpcam.core.data.model.SampleFormat
import com.rejeq.cpcam.core.stream.jni.FFmpegAudioStreamJni
import com.rejeq.cpcam.core.stream.jni.toFFmpegSampleFormat
import java.nio.ByteBuffer

class FFmpegAudioRelay(private val stream: FFmpegAudioStreamJni) {
    fun start() {
        stream.start()
    }

    fun stop() {
        stream.stop()
    }

    fun send(
        buffer: ByteBuffer,
        ts: Long,
        count: Int,
        sampleRate: Int,
        channelCount: Int,
        format: SampleFormat,
    ) {
        stream.send(
            ts,
            count,
            sampleRate,
            channelCount,
            format.toFFmpegSampleFormat(),
            buffer,
        )
    }
}
