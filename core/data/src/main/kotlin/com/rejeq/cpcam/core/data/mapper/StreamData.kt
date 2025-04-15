package com.rejeq.cpcam.core.data.mapper

import android.util.Log
import com.rejeq.cpcam.core.data.mapper.fromString
import com.rejeq.cpcam.core.data.model.AudioCodec
import com.rejeq.cpcam.core.data.model.AudioConfig
import com.rejeq.cpcam.core.data.model.ObsStreamData
import com.rejeq.cpcam.core.data.model.PixFmt
import com.rejeq.cpcam.core.data.model.Resolution
import com.rejeq.cpcam.core.data.model.SampleFormat
import com.rejeq.cpcam.core.data.model.StreamProtocol
import com.rejeq.cpcam.core.data.model.VideoCodec
import com.rejeq.cpcam.core.data.model.VideoConfig
import com.rejeq.cpcam.data.datastore.AudioCodecProto
import com.rejeq.cpcam.data.datastore.AudioConfigProto
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

fun StreamProtocol.toDataStore() = when (this) {
    StreamProtocol.MPEGTS -> StreamProtocolProto.STREAM_PROTOCOL_MPEGTS
    StreamProtocol.MJPEG -> StreamProtocolProto.STREAM_PROTOCOL_MJPEG
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

fun VideoCodec.toDataStore() = when (this) {
    VideoCodec.H264 -> VideoCodecProto.VIDEO_CODEC_H264
    VideoCodec.MJPEG -> VideoCodecProto.VIDEO_CODEC_MJPEG
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

fun AudioCodecProto?.fromDataStore() = when (this) {
    null,
    AudioCodecProto.UNRECOGNIZED,
    AudioCodecProto.AUDIO_CODEC_UNSPECIFIED,
    -> {
        AudioCodec.AAC
    }

    AudioCodecProto.AUDIO_CODEC_AAC -> AudioCodec.AAC
    AudioCodecProto.AUDIO_CODEC_OPUS -> AudioCodec.OPUS
    AudioCodecProto.AUDIO_CODEC_MP3 -> AudioCodec.MP3
}

fun AudioCodec.toDataStore() = when (this) {
    AudioCodec.AAC -> AudioCodecProto.AUDIO_CODEC_AAC
    AudioCodec.OPUS -> AudioCodecProto.AUDIO_CODEC_OPUS
    AudioCodec.MP3 -> AudioCodecProto.AUDIO_CODEC_MP3
}

fun AudioConfigProto.fromDataStore(): AudioConfig {
    val codec = takeIf { it.hasCodec() }?.codec?.fromDataStore()
    val bitRate = takeIf { it.hasBitRate() }?.bitRate
    val sampleRate = takeIf { it.hasSampleRate() }?.sampleRate
    val channelCount = takeIf { it.hasChannelCount() }?.channelCount
    val format = takeIf { it.hasSampleFormat() }?.sampleFormat?.let {
        SampleFormat.fromString(it)
    }

    return AudioConfig(
        codecName = codec,
        bitrate = bitRate,
        format = format,
        sampleRate = sampleRate,
        channelCount = channelCount,
    )
}

fun AudioConfig.toDataStore(): AudioConfigProto {
    val builder = AudioConfigProto.newBuilder().also {
        codecName?.let { codecName ->
            it.setCodec(codecName.toDataStore())
        }

        bitrate?.let { bitrate ->
            it.setBitRate(bitrate)
        }

        sampleRate?.let { sampleRate ->
            it.setSampleRate(sampleRate)
        }

        format?.let { format ->
            it.setSampleFormat(format.toString())
        }

        channelCount?.let { channelCount ->
            it.setChannelCount(channelCount)
        }
    }

    return builder.build()
}

fun SampleFormat.Companion.fromString(sampleFormat: String): SampleFormat? =
    try {
        SampleFormat.valueOf(sampleFormat)
    } catch (e: IllegalArgumentException) {
        Log.d(TAG, "Unable to parse pix_fmt '$sampleFormat': ${e.message}")
        null
    }

fun ObsStreamDataProto.fromDataStore() = ObsStreamData(
    protocol = this.streamProtocol.fromDataStore(),
    host = this.streamHost,
    videoConfig = this.videoConfig.fromDataStore(),
    audioConfig = this.audioConfig.fromDataStore(),
)

fun ObsStreamData.toDataStore(): ObsStreamDataProto {
    val builder = ObsStreamDataProto.newBuilder().also {
        it.streamHost = this.host
        it.streamProtocol = this.protocol.toDataStore()
        it.videoConfig = this.videoConfig.toDataStore()
        it.audioConfig = this.audioConfig.toDataStore()
    }

    return builder.build()
}

private const val TAG = "StreamDataMapper"
