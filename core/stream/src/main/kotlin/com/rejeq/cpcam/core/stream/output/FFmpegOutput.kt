package com.rejeq.cpcam.core.stream.output

import android.util.Log
import com.rejeq.cpcam.core.data.model.StreamProtocol
import com.rejeq.cpcam.core.data.model.VideoConfig
import com.rejeq.cpcam.core.stream.StreamError
import com.rejeq.cpcam.core.stream.StreamErrorKind
import com.rejeq.cpcam.core.stream.jni.FFmpegOutputJni
import com.rejeq.cpcam.core.stream.jni.FFmpegVideoStreamJni
import com.rejeq.cpcam.core.stream.jni.toFFmpegConfig
import com.rejeq.cpcam.core.stream.jni.toFFmpegString

internal class FFmpegOutput(val protocol: StreamProtocol, host: String) {
    private var detail: FFmpegOutputJni? =
        FFmpegOutputJni(protocol.toFFmpegString(), host)

    fun makeVideoStream(config: VideoConfig): FFmpegVideoStreamJni? {
        val config = config.toFFmpegConfig()
        if (config == null) {
            Log.e(TAG, "Unable create encoder: Invalid video config")
            return null
        }

        return detail?.makeVideoStream(config)
    }

    fun open(): StreamErrorKind? {
        val detail = detail
        if (detail == null) {
            Log.e(TAG, "Unable to open: Invalid state")
            return StreamError.InvalidState.toStreamError()
        }

        return detail.open()?.toStreamError()
    }

    fun close(): StreamErrorKind? {
        val detail = detail
        if (detail == null) {
            Log.e(TAG, "Unable to close: Invalid state")
            return StreamError.InvalidState.toStreamError()
        }

        return detail.close()?.toStreamError()
    }

    fun destroy() {
        detail?.destroy()
        detail = null
    }
}

private const val TAG = "FFmpegOutput"
