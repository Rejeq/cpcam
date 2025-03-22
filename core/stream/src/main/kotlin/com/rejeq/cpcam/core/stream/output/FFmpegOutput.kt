package com.rejeq.cpcam.core.stream.output

import android.util.Log
import com.rejeq.cpcam.core.data.model.StreamProtocol
import com.rejeq.cpcam.core.data.model.VideoConfig
import com.rejeq.cpcam.core.stream.StreamResult
import com.rejeq.cpcam.core.stream.jni.FFmpegOutputJni
import com.rejeq.cpcam.core.stream.jni.toFFmpegConfig
import com.rejeq.cpcam.core.stream.jni.toFFmpegString
import com.rejeq.cpcam.core.stream.relay.FFmpegVideoRelay
import com.rejeq.cpcam.core.stream.relay.VideoRelay
import com.rejeq.cpcam.core.stream.target.VideoTarget

internal class FFmpegOutput(val protocol: StreamProtocol, host: String) {
    private val detail = FFmpegOutputJni(protocol.toFFmpegString(), host)
    private var videoTarget: VideoTarget? = null

    fun makeVideoRelay(config: VideoConfig, target: VideoTarget): VideoRelay? {
        val config = config.toFFmpegConfig()
        if (config == null) {
            Log.e(TAG, "Unable create encoder: Invalid video config")
            return null
        }

        val stream = detail.makeVideoStream(config)
        val relay = FFmpegVideoRelay(stream, config.width, config.height)

        target.setRelay(relay)
        videoTarget = target

        return relay
    }

    fun open(): StreamResult<Unit> {
        detail.open()
        videoTarget?.start()

        // TODO: Proper error handling
        return StreamResult.Success(Unit)
    }

    fun close(): StreamResult<Unit> {
        videoTarget?.stop()
        detail.close()

        // TODO: Proper error handling
        return StreamResult.Success(Unit)
    }
}

private const val TAG = "FFmpegOutput"
