package com.rejeq.cpcam.core.stream.jni

import com.rejeq.cpcam.core.stream.StreamErrorKind

// NOTE: Keep sync with jni StreamError
enum class StreamError(val code: Int) {
    // General errors
    Unknown(-1),
    InvalidArgument(-2),
    InvalidState(-3),

    // FFmpeg specific errors
    FFmpegAllocFailed(-100),
    FFmpegCodecNotFound(-101),
    FFmpegCodecOpenFailed(-102),
    FFmpegStreamCreationFailed(-103),
    FFmpegStreamParametersFailed(-104),
    FFmpegWriteFailed(-105),

    // Video specific errors
    VideoInvalidFormat(-200),
    VideoInvalidResolution(-201),
    VideoInvalidPixelFormat(-202),
    VideoInvalidPlaneCount(-203),
    VideoInvalidStride(-204),
    ;

    fun toStreamError(): StreamErrorKind = StreamErrorKind.FFmpegError(this)

    companion object {
        fun fromCode(code: Int): StreamError? = entries.find { it.code == code }
    }
}
