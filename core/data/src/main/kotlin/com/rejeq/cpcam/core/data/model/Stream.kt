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
 * Represents the configuration for a video relay.
 *
 * Defines parameters that a video relay should adhere to when passing
 * data to a video stream.
 *
 * @property framerate The target frame rate
 * @property resolution The resolution of images
 */
data class VideoRelayConfig(val framerate: Int?, val resolution: Resolution?)

/**
 * Supported audio codecs.
 */
enum class AudioCodec {
    AAC,
    OPUS,
    MP3,
}

/**
 * Enumerates the supported audio encoding formats.
 */
enum class SampleFormat {
    PCM_S16LE,
    PCM_S32LE,
    PCM_F32LE,
    ;

    companion object
}

/**
 * Configuration for audio input or output.
 *
 * @property codecName The audio codec to be used for encoding
 * @property bitrate The target bitrate for the encoded audio
 * @property sampleRate The sampling rate of the audio data, in Hz
 * @property format The encoding format of the audio data.
 * @property channelCount The number of audio channels.
 *           Common values are 1 (mono) or 2 (stereo).
 */
data class AudioConfig(
    val codecName: AudioCodec?,
    val bitrate: Int?,
    val sampleRate: Int?,
    val format: SampleFormat?,
    val channelCount: Int?,
)

/**
 * Represents the configuration for a audio relay.
 *
 * Defines parameters that a audio relay should adhere to when passing
 * data to a audio stream.
 *
 * @property sampleRate The sampling rate of the audio data, in Hz
 */
data class AudioRelayConfig(val sampleRate: Int?)

/**
 * Represents the data required to connect to and stream from an OBS instance.
 *
 * @property protocol The streaming protocol to use.
 * @property host Where stream will be served, obs must connect to this address
 * @property videoConfig The configuration settings for the video stream
 * @property audioConfig The configuration settings for the audio stream
 */
data class ObsStreamData(
    val protocol: StreamProtocol,
    val host: String,
    val videoConfig: VideoConfig,
    val audioConfig: AudioConfig,
)
