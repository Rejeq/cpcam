package com.rejeq.cpcam.core.stream.jni

import com.rejeq.cpcam.core.data.model.PixFmt

// NOTE: Keep sync with jni PixFmt
enum class FFmpegPixFmt {
    YUV420P,
    YUV444P,
    NV12,
    NV21,
    RGBA,
    RGB24,
}

fun PixFmt.toFFmpegPixFmt() = when (this) {
    PixFmt.YUV420P -> FFmpegPixFmt.YUV420P
    PixFmt.YUV444P -> FFmpegPixFmt.YUV444P
    PixFmt.NV12 -> FFmpegPixFmt.NV12
    PixFmt.NV21 -> FFmpegPixFmt.NV21
    PixFmt.RGBA -> FFmpegPixFmt.RGBA
    PixFmt.RGBA24 -> FFmpegPixFmt.RGB24
}
