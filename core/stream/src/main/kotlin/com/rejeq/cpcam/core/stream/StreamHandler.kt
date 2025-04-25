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
    val videoStreamConfig: VideoStreamConfig?,
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

    fun setVideoRelayConfig(config: VideoRelayConfig): StreamErrorKind? =
        synchronized(this) {
            if (videoStreamConfig == null) {
                Log.w(
                    TAG,
                    "Unable to set VideoConfig: Does not has videoStreamConfig",
                )
                return StreamErrorKind.NoVideoConfig
            }

            if (config == oldVideoRelayConfig) {
                Log.w(TAG, "Unable to set VideoConfig: Has same config")
                return null
            }

            if (videoStream == null) {
                Log.e(
                    TAG,
                    "Unable to set VideoConfig: Does not has video stream",
                )
                return StreamErrorKind.InvalidVideoStream
            }

            val target = videoStreamConfig.target
            val res = config.resolution
            if (res != null) {
                val relay =
                    FFmpegVideoRelay(videoStream!!, res.width, res.height)

                target.setRelay(relay)
            }

            val framerate = config.framerate
            if (framerate != null) {
                target.setFramerate(Range<Int>(framerate, framerate))
            }

            oldVideoRelayConfig = config
            return null
        }

    fun start(): StreamErrorKind? = synchronized(this) {
        output.open()?.let { errorKind ->
            return errorKind
        }

        videoStreamConfig?.target?.start()
        return null
    }

    fun stop(): StreamErrorKind? = synchronized(this) {
        videoStreamConfig?.target?.stop()

        output.close()?.let { errorKind ->
            return errorKind
        }

        return null
    }
}

class VideoStreamConfig(val target: VideoTarget, val data: VideoConfig)

private const val TAG = "StreamHandler"
