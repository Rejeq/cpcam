#pragma once

#include <cstdint>
#include <string>

#include <jni.h>

#include "Format.h"
#include "JniUtils.h"

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

struct AudioConfig {
public:
    // obj must be AudioConfig class
    static AudioConfig build(JNIEnv *env, jobject obj);

    std::string codec_name;
    SampleFormat format;
    int64_t bitrate;
    int sample_rate;
    int channel_count;
};
