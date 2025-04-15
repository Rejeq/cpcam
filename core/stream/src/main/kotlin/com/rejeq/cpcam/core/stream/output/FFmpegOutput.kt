package com.rejeq.cpcam.core.stream.output

import android.util.Log
import com.rejeq.cpcam.core.data.model.AudioConfig
import com.rejeq.cpcam.core.data.model.StreamProtocol
import com.rejeq.cpcam.core.data.model.VideoConfig
import com.rejeq.cpcam.core.stream.StreamResult
import com.rejeq.cpcam.core.stream.jni.FFmpegAudioStreamJni
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

    fun makeAudioStream(config: AudioConfig): FFmpegAudioStreamJni? {
        val config = config.toFFmpegConfig()
        if (config == null) {
            Log.e(TAG, "Unable create encoder: Invalid audio config")
            return null
        }

        return detail?.makeAudioStream(config)
    }

    fun open(): StreamResult<Unit> {
        detail?.open()

        // TODO: Proper error handling
        return StreamResult.Success(Unit)
    }

    fun close(): StreamResult<Unit> {
        detail?.close()

        // TODO: Proper error handling
        return StreamResult.Success(Unit)
    }

    fun destroy() {
        detail?.destroy()
        detail = null
    }
}

private const val TAG = "FFmpegOutput"
