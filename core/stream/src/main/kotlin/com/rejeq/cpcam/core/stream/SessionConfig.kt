package com.rejeq.cpcam.core.stream

import com.rejeq.cpcam.core.data.model.StreamProtocol
import com.rejeq.cpcam.core.data.model.VideoConfig
import com.rejeq.cpcam.core.stream.target.Target
import com.rejeq.cpcam.core.stream.target.TargetState
import com.rejeq.cpcam.core.stream.target.VideoTarget

/**
 * Represents the configuration for a session.
 *
 * Defines the network settings and optional video encoding parameters
 * for setting up a video stream.
 *
 * @property protocol The network protocol used for streaming.
 * @property host The destination address for the stream.
 * @property videoStreamConfig Video configuration or null if without video
 */
data class SessionConfig(
    val protocol: StreamProtocol,
    val host: String,
    val videoStreamConfig: VideoStreamConfig?,
)

/**
 * Represents the configuration for a video source and its encoding settings.
 *
 * Associates a video source (target) with the encoding parameters
 * that should be used when streaming frames from that source.
 *
 * @property target The video source from which frames originate.
 * @property data The encoding settings applied to the video stream.
 */
data class VideoStreamConfig(val target: VideoTarget, val data: VideoConfig)

data class Stream<T : TargetState>(val target: Target<T>, val state: T)
