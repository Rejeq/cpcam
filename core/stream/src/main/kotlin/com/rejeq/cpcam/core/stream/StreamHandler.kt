package com.rejeq.cpcam.core.stream

import android.util.Log
import android.util.Range
import com.rejeq.cpcam.core.data.model.PixFmt
import com.rejeq.cpcam.core.data.model.StreamProtocol
import com.rejeq.cpcam.core.data.model.VideoCodec
import com.rejeq.cpcam.core.stream.jni.FFmpegVideoStreamJni
import com.rejeq.cpcam.core.stream.output.FFmpegOutput
import com.rejeq.cpcam.core.stream.relay.FFmpegVideoRelay

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

    fun destroy() = synchronized(this) {
        output.destroy()
    }

    companion object {
        fun getSupportedCodecs(): List<VideoCodec> =
            FFmpegOutput.getSupportedCodecs()

        fun getSupportedFormats(codec: VideoCodec): List<PixFmt> =
            FFmpegOutput.getSupportedFormats(codec)
    }
}

private const val TAG = "StreamHandler"
