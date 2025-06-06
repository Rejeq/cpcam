package com.rejeq.cpcam.core.stream.jni

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
    VideoCodec.VP8 -> "vp8_mediacodec"
    VideoCodec.VP9 -> "vp9_mediacodec"
    VideoCodec.AV1 -> "av1_mediacodec"
    VideoCodec.MPEG4 -> "mpeg4_mediacodec"
    VideoCodec.HEVC -> "hevc_mediacodec"
    VideoCodec.MJPEG -> "mjpeg"
}

fun StreamProtocol.toFFmpegString() = when (this) {
    StreamProtocol.MPEGTS -> "mpegts"
    StreamProtocol.MJPEG -> "mjpeg"
    StreamProtocol.SMJPEG -> "smjpeg"
    StreamProtocol.RTSP -> "rtsp"
    StreamProtocol.RTP -> "rtp"
    StreamProtocol.RTP_MPEGTS -> "rtp_mpegts"
    StreamProtocol.HLS -> "hls"
}
