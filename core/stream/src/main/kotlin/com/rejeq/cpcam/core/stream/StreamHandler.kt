package com.rejeq.cpcam.core.stream

import com.rejeq.cpcam.core.data.model.StreamProtocol
import com.rejeq.cpcam.core.data.model.VideoConfig
import com.rejeq.cpcam.core.stream.output.FFmpegOutput
import com.rejeq.cpcam.core.stream.target.VideoTarget

class StreamHandler(
    protocol: StreamProtocol,
    host: String,
    videoConfig: VideoStreamConfig?,
) {
    private val output by lazy {
        val output = FFmpegOutput(protocol, host)

        videoConfig?.let {
            output.makeVideoRelay(it.data, it.target)
        }

        output
    }

    fun start(): StreamResult<Unit> = output.open()

    fun stop(): StreamResult<Unit> = output.close()
}

class VideoStreamConfig(val target: VideoTarget, val data: VideoConfig)
