package com.rejeq.cpcam.core.stream

import com.rejeq.cpcam.core.data.model.Resolution
import com.rejeq.cpcam.core.data.model.StreamProtocol
import com.rejeq.cpcam.core.data.model.VideoConfig
import com.rejeq.cpcam.core.stream.target.VideoTarget

/**
 * Represents the configuration for a stream.
 *
 * Defines the network settings and optional video encoding parameters
 * for setting up a video stream.
 *
 * @property protocol The network protocol used for streaming.
 * @property host The destination address for the stream.
 * @property videoStreamConfig Video configuration or null if without video
 */
data class StreamConfig(
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
class VideoStreamConfig(val target: VideoTarget, val data: VideoConfig)

/**
 * Defines the configuration settings for a video relay.
 *
 * Unlike [VideoStreamConfig.data], this configuration can be updated
 * dynamically after the stream has started.
 * Since it only affects the relay, the underlying stream settings might become
 * inconsistent - for example, the stream may expect a framerate of 30 FPS,
 * but 60 FPS frames could be relayed instead.
 *
 * @property framerate The desired frame rate for the relay,
 *           or null to leave unchanged.
 * @property resolution The desired resolution for the relay,
 *           or null to leave unchanged.
 */
data class VideoRelayConfig(val framerate: Int?, val resolution: Resolution?)
