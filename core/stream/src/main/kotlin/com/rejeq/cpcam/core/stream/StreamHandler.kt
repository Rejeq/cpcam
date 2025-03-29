package com.rejeq.cpcam.core.stream

import android.util.Log
import android.util.Range
import com.rejeq.cpcam.core.data.model.StreamProtocol
import com.rejeq.cpcam.core.data.model.VideoConfig
import com.rejeq.cpcam.core.data.model.VideoRelayConfig
import com.rejeq.cpcam.core.stream.jni.FFmpegVideoStreamJni
import com.rejeq.cpcam.core.stream.output.FFmpegOutput
import com.rejeq.cpcam.core.stream.relay.FFmpegVideoRelay
import com.rejeq.cpcam.core.stream.target.VideoTarget

class StreamHandler(
    val protocol: StreamProtocol,
    val host: String,
    private val videoStreamConfig: VideoStreamConfig?,
) {
    private var oldVideoRelayConfig: VideoRelayConfig? = null
    private var output = FFmpegOutput(protocol, host)

    private val videoStream: FFmpegVideoStreamJni? by lazy {
        videoStreamConfig?.data?.let { config ->
            val stream = output.makeVideoStream(config)
            if (stream == null) {
                Log.e(TAG, "Unable to make video stream")
                return@let null
            }

            stream
        }
    }

    // TODO: Wrap Boolean to custom result sealed class
    // Returns true on failure
    // Returns false on success
    fun setVideoRelayConfig(config: VideoRelayConfig): Boolean =
        synchronized(this) {
            if (videoStreamConfig == null) {
                Log.w(
                    TAG,
                    "Unable to set VideoConfig: Does not has videoStreamConfig",
                )
                return true
            }

            if (config == oldVideoRelayConfig) {
                Log.w(TAG, "Unable to set VideoConfig: Has same config")
                return true
            }

            if (videoStream == null) {
                Log.e(
                    TAG,
                    "Unable to set VideoConfig: Does not has video stream",
                )
                return true
            }

            val res = config.resolution
            if (res == null) {
                Log.e(TAG, "Unable to set VideoConfig: Missing resolution")
                return true
            }

            val target = videoStreamConfig.target
            val relay = FFmpegVideoRelay(videoStream!!, res.width, res.height)

            target.setRelay(relay)

            val framerate = config.framerate
            if (framerate != null) {
                target.setFramerate(Range<Int>(framerate, framerate))
            }

            oldVideoRelayConfig = config
            return false
        }

    fun start(): StreamResult<Unit> = synchronized(this) {
        output.open()
        videoStreamConfig?.target?.start()

        return StreamResult.Success(Unit)
    }

    fun stop(): StreamResult<Unit> = synchronized(this) {
        videoStreamConfig?.target?.stop()
        output.close()

        return StreamResult.Success(Unit)
    }
}

class VideoStreamConfig(val target: VideoTarget, val data: VideoConfig)

private const val TAG = "StreamHandler"
