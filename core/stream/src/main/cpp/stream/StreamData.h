#pragma once

#include <cstdint>

#include "Format.h"

struct FrameData {
    int64_t ts; // timestamp
    int32_t width;
    int32_t height;

    uint8_t *buff[4];
    int32_t buff_stride[4];
};

struct AudioData {
    int64_t ts; // timestamp
    int samples_count;
    int sample_rate;
    int channel_count;

    uint8_t *buff[4];
    int32_t buff_stride[4];
};
