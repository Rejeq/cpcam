package com.rejeq.cpcam.core.stream

import com.rejeq.cpcam.core.data.model.VideoConfig

class StreamHolder {
    private var currConfig: StreamConfig? = null
    var current: StreamHandler? = null
        private set

    fun getConfigured(config: StreamConfig): StreamHandler =
        synchronized(this) {
            val handler = current
            when {
                handler == null -> configure(config)
                config != currConfig -> configure(config)
                else -> handler
            }
        }

    private fun configure(config: StreamConfig): StreamHandler {
        if (current != null) {
            current?.destroy()
        }

        currConfig = config
        val handler = StreamHandler(
            config.protocol,
            config.host,
            config.videoStreamConfig,
        )

        config.videoStreamConfig?.data?.let { videoConfig ->
            // TODO: Error handling
            handler.configureVideoRelay(videoConfig)
        }

        this.current = handler
        return handler
    }
}

private fun StreamHandler.configureVideoRelay(config: VideoConfig) =
    setVideoRelayConfig(
        VideoRelayConfig(
            framerate = config.framerate,
            resolution = config.resolution,
        ),
    )
