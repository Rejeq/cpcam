package com.rejeq.cpcam.core.stream.output

import android.util.Log
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapError
import com.rejeq.cpcam.core.data.model.PixFmt
import com.rejeq.cpcam.core.data.model.StreamProtocol
import com.rejeq.cpcam.core.data.model.VideoCodec
import com.rejeq.cpcam.core.data.model.VideoConfig
import com.rejeq.cpcam.core.stream.StreamErrorKind
import com.rejeq.cpcam.core.stream.jni.FFmpegOutputJni
import com.rejeq.cpcam.core.stream.jni.FFmpegPixFmt
import com.rejeq.cpcam.core.stream.jni.StreamError
import com.rejeq.cpcam.core.stream.jni.toFFmpegCodecName
import com.rejeq.cpcam.core.stream.jni.toFFmpegConfig
import com.rejeq.cpcam.core.stream.jni.toFFmpegString
import com.rejeq.cpcam.core.stream.jni.toPixFmt
import com.rejeq.cpcam.core.stream.relay.FFmpegVideoRelay
import com.rejeq.cpcam.core.stream.relay.VideoRelay

internal class FFmpegOutput(val protocol: StreamProtocol, host: String) :
    StreamOutput {
    private var detail: FFmpegOutputJni? =
        FFmpegOutputJni(protocol.toFFmpegString(), host)

    override fun makeVideoRelay(
        config: VideoConfig,
    ): Result<VideoRelay, StreamError> {
        val detail = requireNotNull(detail)

        val config = config.toFFmpegConfig()
        if (config == null) {
            Log.e(TAG, "Unable create encoder: Invalid video config")
            return Err(StreamError.InvalidState)
        }

        val relay = detail.makeVideoStream(config).map { stream ->
            FFmpegVideoRelay(stream)
        }

        return relay
    }

    override fun open(): Result<Unit, StreamErrorKind> {
        val detail = requireNotNull(detail)
        return detail.open().mapError { it.toStreamError() }
    }

    override fun close(): Result<Unit, StreamErrorKind> {
        val detail = requireNotNull(detail)
        return detail.close().mapError { it.toStreamError() }
    }

    override fun destroy() {
        detail?.destroy()
        detail = null
    }

    companion object {
        fun getSupportedCodecs(): List<VideoCodec> = VideoCodec.entries
        fun getSupportedFormats(codec: VideoCodec): List<PixFmt> =
            FFmpegOutputJni.getSupportedFormats(codec.toFFmpegCodecName()).map {
                FFmpegPixFmt.entries[it].toPixFmt()
            }
    }
}

private const val TAG = "FFmpegOutput"
