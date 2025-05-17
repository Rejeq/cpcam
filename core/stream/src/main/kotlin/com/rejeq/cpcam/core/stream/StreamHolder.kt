package com.rejeq.cpcam.core.stream

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.flatMap
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.toResultOr
import com.rejeq.cpcam.core.data.model.VideoConfig

class StreamHolder {
    private var currConfig: StreamConfig? = null
    var current: StreamHandler? = null
        private set

    fun getConfigured(
        config: StreamConfig,
    ): Result<StreamHandler, StreamErrorKind> = synchronized(this) {
        val handler = current
        when {
            handler == null -> configure(config)
            config != currConfig -> configure(config)
            else -> Ok(handler)
        }
    }

    private fun configure(
        config: StreamConfig,
    ): Result<StreamHandler, StreamErrorKind> {
        if (current != null) {
            current?.destroy()
        }

        currConfig = config
        val handler = StreamHandler(
            config.protocol,
            config.host,
            config.videoStreamConfig,
        )

        config.videoStreamConfig
            .toResultOr { StreamErrorKind.NoVideoConfig }
            .flatMap { config -> handler.configureVideoRelay(config.data) }
            .onFailure { return Err(it) }

        this.current = handler
        return Ok(handler)
    }
}

private fun StreamHandler.configureVideoRelay(config: VideoConfig) =
    setVideoRelayConfig(
        VideoRelayConfig(
            framerate = config.framerate,
            resolution = config.resolution,
        ),
    )
