package com.rejeq.cpcam.core.stream

import android.util.Log
import android.util.Range
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.flatMap
import com.github.michaelbull.result.get
import com.github.michaelbull.result.getError
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.github.michaelbull.result.toResultOr
import com.rejeq.cpcam.core.data.model.PixFmt
import com.rejeq.cpcam.core.data.model.StreamProtocol
import com.rejeq.cpcam.core.data.model.VideoCodec
import com.rejeq.cpcam.core.stream.jni.FFmpegVideoStreamJni
import com.rejeq.cpcam.core.stream.jni.StreamError
import com.rejeq.cpcam.core.stream.output.FFmpegOutput
import com.rejeq.cpcam.core.stream.relay.FFmpegVideoRelay

class StreamHandler(
    val protocol: StreamProtocol,
    val host: String,
    val videoStreamConfig: VideoStreamConfig?,
) {
    private var oldVideoRelayConfig: VideoRelayConfig? = null
    private var output = FFmpegOutput(protocol, host)

    private val videoStream: Result<FFmpegVideoStreamJni, StreamError> by lazy {
        videoStreamConfig
            .toResultOr { StreamError.InvalidArgument }
            .flatMap { config -> output.makeVideoStream(config.data) }
            .onFailure { Log.e(TAG, "Unable to make video stream") }
    }

    fun setVideoRelayConfig(
        config: VideoRelayConfig,
    ): Result<Unit, StreamErrorKind> = synchronized(this) {
        if (videoStreamConfig == null) {
            Log.w(
                TAG,
                "Unable to set VideoConfig: Does not has videoStreamConfig",
            )
            return Err(StreamErrorKind.NoVideoConfig)
        }

        if (config == oldVideoRelayConfig) {
            Log.w(TAG, "Unable to set VideoConfig: Has same config")
            return Ok(Unit)
        }

        val stream = videoStream.get()
        if (stream == null) {
            Log.e(
                TAG,
                "Unable to set VideoConfig: Does not has video stream " +
                    "(${videoStream.getError()})",
            )
            return Err(StreamErrorKind.InvalidVideoStream)
        }

        val target = videoStreamConfig.target
        val res = config.resolution
        if (res != null) {
            val relay = FFmpegVideoRelay(stream, res.width, res.height)

            target.setRelay(relay)
        }

        val framerate = config.framerate
        if (framerate != null) {
            target.setFramerate(Range<Int>(framerate, framerate))
        }

        oldVideoRelayConfig = config
        return Ok(Unit)
    }

    fun start(): Result<Unit, StreamErrorKind> = synchronized(this) {
        output.open().andThen {
            videoStreamConfig?.target?.start()
            Ok(Unit)
        }
    }

    fun stop(): Result<Unit, StreamErrorKind> = synchronized(this) {
        videoStreamConfig?.target?.stop()

        return output.close()
    }

    fun destroy(): Unit = synchronized(this) {
        videoStream.onSuccess { it.destroy() }
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
