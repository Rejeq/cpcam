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

    StreamProtocolProto.STREAM_PROTOCOL_MPEGTS -> {
        StreamProtocol.MPEGTS
    }

    StreamProtocolProto.STREAM_PROTOCOL_MJPEG -> {
        StreamProtocol.MJPEG
    }
}

fun VideoCodecProto?.fromDataStore() = when (this) {
    null,
    VideoCodecProto.UNRECOGNIZED,
    VideoCodecProto.VIDEO_CODEC_UNSPECIFIED,
    -> {
        VideoCodec.H264
    }

    VideoCodecProto.VIDEO_CODEC_H264 -> {
        VideoCodec.H264
    }

    VideoCodecProto.VIDEO_CODEC_MJPEG -> {
        VideoCodec.MJPEG
    }
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

private const val TAG = "StreamDataMapper"
