package com.rejeq.cpcam.core.stream.output

import com.github.michaelbull.result.Result
import com.rejeq.cpcam.core.data.model.VideoConfig
import com.rejeq.cpcam.core.stream.StreamErrorKind
import com.rejeq.cpcam.core.stream.jni.FFmpegVideoStreamJni
import com.rejeq.cpcam.core.stream.jni.StreamError
import com.rejeq.cpcam.core.stream.relay.VideoRelay

interface StreamOutput {
    fun makeVideoRelay(config: VideoConfig): Result<VideoRelay, StreamError>

    fun open(): Result<Unit, StreamErrorKind>
    fun close(): Result<Unit, StreamErrorKind>

    fun destroy()
}
