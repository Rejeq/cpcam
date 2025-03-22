#pragma once

#include <cstdint>

#include "PixFmt.h"

struct FrameData {
    int64_t ts; // timestamp
    int32_t width;
    int32_t height;

    uint8_t *buff[4];
    int32_t buff_stride[4];
};
