#pragma once

enum class PixFmt {
    Unknown = -1, // Must never sent to the kotlin side
    YUV420P,
    YUV444P,
    NV12,
    NV21,
    RGBA,
    RGB24,
};
