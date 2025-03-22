#pragma once

#include <cstdint>
#include <string>

#include <jni.h>

#include "JniUtils.h"
#include "PixFmt.h"

struct VideoConfig {
   public:
    // obj must be VideoConfig class
    static VideoConfig build(JNIEnv *env, jobject obj);

    std::string codec_name;
    PixFmt pix_fmt;
    int64_t bitrate;
    int framerate;
    int width;
    int height;
};
