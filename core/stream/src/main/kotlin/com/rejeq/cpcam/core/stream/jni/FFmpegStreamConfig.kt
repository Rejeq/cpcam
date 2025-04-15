package com.rejeq.cpcam.core.stream.jni

import com.rejeq.cpcam.core.data.model.AudioCodec
import com.rejeq.cpcam.core.data.model.AudioConfig
import com.rejeq.cpcam.core.data.model.StreamProtocol
import com.rejeq.cpcam.core.data.model.VideoCodec
import com.rejeq.cpcam.core.data.model.VideoConfig

// NOTE: Keep sync with jni VideoConfig
class FFmpegVideoConfig(
    val codecName: String,
    val pixFmt: FFmpegPixFmt,
    val bitrate: Long,
    val framerate: Int,
    val width: Int,
    val height: Int,
)

fun VideoConfig.toFFmpegConfig(): FFmpegVideoConfig? {
    val codec = codecName?.toFFmpegCodecName() ?: return null
    val format = pixFmt?.toFFmpegPixFmt() ?: return null
    val bitrate = bitrate ?: return null
    val framerate = framerate ?: return null
    val res = resolution ?: return null

    return FFmpegVideoConfig(
        codecName = codec,
        pixFmt = format,
        bitrate = bitrate.toLong(),
        framerate = framerate,
        width = res.width,
        height = res.height,
    )
}

fun VideoCodec.toFFmpegCodecName() = when (this) {
    VideoCodec.H264 -> "h264_mediacodec"
    VideoCodec.MJPEG -> "mjpeg"
}

fun StreamProtocol.toFFmpegString() = when (this) {
    StreamProtocol.MPEGTS -> "mpegts"
    StreamProtocol.MJPEG -> "mjpeg"
}

// NOTE: Keep in sync with jni AudioConfig
class FFmpegAudioConfig(
    val codecName: String,
    val format: FFmpegSampleFormat,
    val bitrate: Long,
    val sampleRate: Int,
    val channelCount: Int,
)

fun AudioConfig.toFFmpegConfig(): FFmpegAudioConfig? {
    val codec = codecName?.toFFmpegCodecName() ?: return null
    val format = format?.toFFmpegSampleFormat() ?: return null
    val bitrate = bitrate ?: return null
    val sampleRate = sampleRate ?: return null
    val channelCount = channelCount ?: return null

    return FFmpegAudioConfig(
        codecName = codec,
        format = format,
        bitrate = bitrate.toLong(),
        sampleRate = sampleRate,
        channelCount = channelCount,
    )
}

fun AudioCodec.toFFmpegCodecName() = when (this) {
    AudioCodec.AAC -> "aac"
    AudioCodec.OPUS -> "opus"
    AudioCodec.MP3 -> "mp3"
}
