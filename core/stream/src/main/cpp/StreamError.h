#pragma once

enum class StreamError {
    Success = 0,

    // General errors
    Unknown = -1,
    InvalidArgument = -2,
    InvalidState = -3,

    // FFmpeg specific errors
    FFmpegAllocFailed = -100,
    FFmpegCodecNotFound = -101,
    FFmpegCodecOpenFailed = -102,
    FFmpegStreamCreationFailed = -103,
    FFmpegStreamParametersFailed = -104,
    FFmpegWriteFailed = -105,

    // Video specific errors
    VideoInvalidFormat = -200,
    VideoInvalidResolution = -201,
    VideoInvalidPixelFormat = -202,
    VideoInvalidPlaneCount = -203,
    VideoInvalidStride = -204,
};
