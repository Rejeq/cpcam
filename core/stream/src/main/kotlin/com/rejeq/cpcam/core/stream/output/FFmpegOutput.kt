package com.rejeq.cpcam.core.stream.output

import android.util.Log
import com.rejeq.cpcam.core.data.model.StreamProtocol
import com.rejeq.cpcam.core.data.model.VideoConfig
import com.rejeq.cpcam.core.stream.StreamErrorKind
import com.rejeq.cpcam.core.stream.jni.FFmpegOutputJni
import com.rejeq.cpcam.core.stream.jni.FFmpegVideoStreamJni
import com.rejeq.cpcam.core.stream.jni.toFFmpegConfig
import com.rejeq.cpcam.core.stream.jni.toFFmpegString

internal class FFmpegOutput(val protocol: StreamProtocol, host: String) :
    StreamOutput {
    private var detail: FFmpegOutputJni? =
        FFmpegOutputJni(protocol.toFFmpegString(), host)

    fun makeVideoStream(config: VideoConfig): FFmpegVideoStreamJni? {
        val detail = requireNotNull(detail)

        val config = config.toFFmpegConfig()
        if (config == null) {
            Log.e(TAG, "Unable create encoder: Invalid video config")
            return null
        }

        return detail.makeVideoStream(config)
    }

    override fun open(): StreamErrorKind? {
        val detail = requireNotNull(detail)
        return detail.open()?.toStreamError()
    }

    override fun close(): StreamErrorKind? {
        val detail = requireNotNull(detail)
        return detail.close()?.toStreamError()
    }

    override fun destroy() {
        detail?.destroy()
        detail = null
    }
}

private const val TAG = "FFmpegOutput"
