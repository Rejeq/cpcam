package com.rejeq.cpcam.core.stream

import android.util.Log
import android.util.Range
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.filterErrors
import com.github.michaelbull.result.filterValues
import com.github.michaelbull.result.getOrElse
import com.rejeq.cpcam.core.stream.output.FFmpegOutput
import com.rejeq.cpcam.core.stream.output.StreamOutput
import com.rejeq.cpcam.core.stream.target.VideoTargetState

class SessionHolder {
    private var currConfig: SessionConfig? = null
    var current: SessionRunner? = null
        private set

    fun getConfigured(
        config: SessionConfig,
    ): Result<SessionRunner, StreamErrorKind> = synchronized(this) {
        val handler = current
        when {
            handler == null -> configure(config)
            config != currConfig -> configure(config)
            else -> Ok(handler)
        }
    }

    private fun configure(
        config: SessionConfig,
    ): Result<SessionRunner, StreamErrorKind> {
        current?.destroy()

        val output = configureStreamOutput(config).getOrElse { err ->
            Log.w(TAG, "Failed to configure stream output: $err")
            return Err(err)
        }

        val streams = configureStreams(config, output)

        streams.filterErrors().forEach {
            Log.w(TAG, "Failed to configure stream: $it")
        }

        val validStreams = streams.filterValues()
        if (validStreams.isEmpty()) {
            return Err(StreamErrorKind.NoVideoConfig)
        }

        val handler = SessionRunner(output, validStreams)
        this.current = handler

        currConfig = config
        return Ok(handler)
    }
}

private fun configureStreamOutput(
    config: SessionConfig,
): Result<StreamOutput, StreamErrorKind> {
    if (config.host.isBlank()) {
        return Err(StreamErrorKind.NoHost)
    }

    val output = FFmpegOutput(config.protocol, config.host)
    return Ok(output)
}

private fun configureStreams(
    config: SessionConfig,
    output: StreamOutput,
): List<Result<Stream<*>, StreamErrorKind>> {
    val videoStream = config.videoStreamConfig?.let { vsConfig ->
        configureVideoStream(vsConfig, output)
    } ?: Err(StreamErrorKind.NoVideoConfig)

    return listOf(videoStream)
}

private fun configureVideoStream(
    config: VideoStreamConfig,
    output: StreamOutput,
): Result<Stream<VideoTargetState>, StreamErrorKind> {
    val relay = output.makeVideoRelay(config.data).getOrElse {
        return Err(StreamErrorKind.FFmpegError(it))
    }
    val framerate = config.data.framerate?.let { framerate ->
        Range(framerate, framerate)
    }

    return Ok(
        Stream(
            target = config.target,
            state = VideoTargetState(true, relay, framerate),
        ),
    )
}

private const val TAG = "StreamHolder"
