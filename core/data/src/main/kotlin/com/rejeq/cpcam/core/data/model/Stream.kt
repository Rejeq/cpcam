package com.rejeq.cpcam.core.data.model

/**
 * Supported streaming protocols.
 */
enum class StreamProtocol {
    MPEGTS,
    MJPEG,
}

/**
 * Supported video codecs.
 *
 * @property H264 H.264/AVC video codec
 * @property MJPEG Motion JPEG video codec
 */
enum class VideoCodec {
    H264,
    MJPEG,
}

/**
 * Represent common pixel formats.
 */
enum class PixFmt {
    YUV420P,
    YUV444P,
    NV12,
    NV21,
    RGBA,
    RGBA24,
    ;

    companion object
}

/**
 * Represents the configuration for a video encoder.
 *
 * Defines parameters that a video encoder should adhere to when encoding a
 * video stream.
 *
 * @property codecName The video codec to be used for encoding
 * @property pixFmt The pixel format of the input video frames
 * @property bitrate The target bitrate for the encoded video
 * @property framerate The target frame rate of the encoded video
 * @property resolution The resolution of the encoded video frames, in pixels.
 */
data class VideoConfig(
    val codecName: VideoCodec?,
    val pixFmt: PixFmt?,
    val bitrate: Int?,
    val framerate: Int?,
    val resolution: Resolution?,
)

/**
 * Represents the data required to connect to and stream from an OBS instance.
 *
 * @property protocol The streaming protocol to use.
 * @property host Where stream will be served, obs must connect to this address
 * @property videoConfig The configuration settings for the video stream
 */
data class ObsStreamData(
    val protocol: StreamProtocol,
    val host: String,
    val videoConfig: VideoConfig,
)
