package com.rejeq.cpcam.core.data.mapper

import android.util.Log
import com.rejeq.cpcam.core.data.model.ObsStreamData
import com.rejeq.cpcam.core.data.model.PixFmt
import com.rejeq.cpcam.core.data.model.Resolution
import com.rejeq.cpcam.core.data.model.StreamProtocol
import com.rejeq.cpcam.core.data.model.VideoCodec
import com.rejeq.cpcam.core.data.model.VideoConfig
import com.rejeq.cpcam.data.datastore.ObsStreamDataProto
import com.rejeq.cpcam.data.datastore.StreamProtocolProto
import com.rejeq.cpcam.data.datastore.VideoCodecProto
import com.rejeq.cpcam.data.datastore.VideoConfigProto

fun StreamProtocolProto?.fromDataStore() = when (this) {
    null,
    StreamProtocolProto.UNRECOGNIZED,
    StreamProtocolProto.STREAM_PROTOCOL_UNSPECIFIED,
    -> {
        StreamProtocol.MPEGTS
    }

    StreamProtocolProto.STREAM_PROTOCOL_MPEGTS -> StreamProtocol.MPEGTS
    StreamProtocolProto.STREAM_PROTOCOL_MJPEG -> StreamProtocol.MJPEG
    StreamProtocolProto.STREAM_PROTOCOL_SMJPEG -> StreamProtocol.SMJPEG
    StreamProtocolProto.STREAM_PROTOCOL_RTSP -> StreamProtocol.RTSP
    StreamProtocolProto.STREAM_PROTOCOL_RTP -> StreamProtocol.RTP
    StreamProtocolProto.STREAM_PROTOCOL_RTP_MPEGTS -> StreamProtocol.RTP_MPEGTS
    StreamProtocolProto.STREAM_PROTOCOL_HLS -> StreamProtocol.HLS
}

fun StreamProtocol.toDataStore() = when (this) {
    StreamProtocol.MPEGTS -> StreamProtocolProto.STREAM_PROTOCOL_MPEGTS
    StreamProtocol.MJPEG -> StreamProtocolProto.STREAM_PROTOCOL_MJPEG
    StreamProtocol.SMJPEG -> StreamProtocolProto.STREAM_PROTOCOL_SMJPEG
    StreamProtocol.RTSP -> StreamProtocolProto.STREAM_PROTOCOL_RTSP
    StreamProtocol.RTP -> StreamProtocolProto.STREAM_PROTOCOL_RTP
    StreamProtocol.RTP_MPEGTS -> StreamProtocolProto.STREAM_PROTOCOL_RTP_MPEGTS
    StreamProtocol.HLS -> StreamProtocolProto.STREAM_PROTOCOL_HLS
}

fun VideoCodecProto?.fromDataStore() = when (this) {
    null,
    VideoCodecProto.UNRECOGNIZED,
    VideoCodecProto.VIDEO_CODEC_UNSPECIFIED,
    -> {
        VideoCodec.H264
    }

    VideoCodecProto.VIDEO_CODEC_H264 -> VideoCodec.H264
    VideoCodecProto.VIDEO_CODEC_MJPEG -> VideoCodec.MJPEG
    VideoCodecProto.VIDEO_CODEC_VP8 -> VideoCodec.VP8
    VideoCodecProto.VIDEO_CODEC_VP9 -> VideoCodec.VP9
    VideoCodecProto.VIDEO_CODEC_AV1 -> VideoCodec.AV1
    VideoCodecProto.VIDEO_CODEC_MPEG4 -> VideoCodec.MPEG4
    VideoCodecProto.VIDEO_CODEC_HEVC -> VideoCodec.HEVC
}

fun VideoCodec.toDataStore() = when (this) {
    VideoCodec.H264 -> VideoCodecProto.VIDEO_CODEC_H264
    VideoCodec.MJPEG -> VideoCodecProto.VIDEO_CODEC_MJPEG
    VideoCodec.VP8 -> VideoCodecProto.VIDEO_CODEC_VP8
    VideoCodec.VP9 -> VideoCodecProto.VIDEO_CODEC_VP9
    VideoCodec.AV1 -> VideoCodecProto.VIDEO_CODEC_AV1
    VideoCodec.MPEG4 -> VideoCodecProto.VIDEO_CODEC_MPEG4
    VideoCodec.HEVC -> VideoCodecProto.VIDEO_CODEC_HEVC
}

fun VideoConfigProto.fromDataStore(): VideoConfig {
    val resolution = takeIf { it.hasResolution() }?.resolution?.let {
        Resolution.fromString(it) ?: run {
            Log.w(TAG, "Unable to parse resolution '$it'")
            null
        }
    }

    val pixFmt = takeIf { it.hasPixFmt() }?.pixFmt?.let {
        PixFmt.fromString(it)
    }

    val codec = takeIf { it.hasCodec() }?.codec?.fromDataStore()
    val bitRate = takeIf { it.hasBitRate() }?.bitRate
    val framerate = takeIf { it.hasFramerate() }?.framerate

    return VideoConfig(
        codecName = codec,
        pixFmt = pixFmt,
        bitrate = bitRate,
        framerate = framerate,
        resolution = resolution,
    )
}

fun VideoConfig.toDataStore(): VideoConfigProto {
    val builder = VideoConfigProto.newBuilder().also {
        resolution?.let { resolution ->
            it.setResolution(resolution.toString())
        }

        pixFmt?.let { pixFmt ->
            it.setPixFmt(pixFmt.toString())
        }

        codecName?.let { codecName ->
            it.setCodec(codecName.toDataStore())
        }

        bitrate?.let { bitrate ->
            it.setBitRate(bitrate)
        }

        framerate?.let { framerate ->
            it.setFramerate(framerate)
        }
    }

    return builder.build()
}

fun PixFmt.Companion.fromString(pixFmt: String): PixFmt? = try {
    PixFmt.valueOf(pixFmt)
} catch (e: IllegalArgumentException) {
    Log.d(TAG, "Unable to parse pix_fmt '$pixFmt': ${e.message}")
    null
}

fun ObsStreamDataProto.fromDataStore() = ObsStreamData(
    protocol = this.streamProtocol.fromDataStore(),
    host = this.streamHost,
    videoConfig = this.videoConfig.fromDataStore(),
)

fun ObsStreamData.toDataStore(): ObsStreamDataProto {
    val builder = ObsStreamDataProto.newBuilder().also {
        it.streamHost = this.host
        it.streamProtocol = this.protocol.toDataStore()
        it.videoConfig = this.videoConfig.toDataStore()
    }

    return builder.build()
}

private const val TAG = "StreamDataMapper"
