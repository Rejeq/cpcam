#include "JniUtils.h"

#include <cassert>
#include <jni.h>

extern "C" {
#include <libavcodec/codec.h>
#include <libavutil/log.h>
}

#define LOG_TAG "common"
#include "Log.h"

static JavaVM *g_jvm = nullptr;

JavaVM *get_jvm() {
    assert(g_jvm != nullptr);
    return g_jvm;
}

std::string to_string(JNIEnv *env, jstring str) {
    // TODO: Maybe use GetStringCritical?
    const char *camera_id = env->GetStringUTFChars(str, nullptr);
    std::string out(camera_id);
    env->ReleaseStringUTFChars(str, camera_id);

    return out;
}

int to_android_log_level(int ffmpeg_level) {
    switch (ffmpeg_level) {
        case AV_LOG_QUIET: return ANDROID_LOG_SILENT;
        case AV_LOG_PANIC:
        case AV_LOG_FATAL: return ANDROID_LOG_FATAL;
        case AV_LOG_ERROR: return ANDROID_LOG_ERROR;
        case AV_LOG_WARNING: return ANDROID_LOG_WARN;
        case AV_LOG_INFO:
        case AV_LOG_VERBOSE: return ANDROID_LOG_INFO;
        case AV_LOG_DEBUG: return ANDROID_LOG_DEBUG;
        case AV_LOG_TRACE: return ANDROID_LOG_VERBOSE;
        default: break;
    }

    return -1;
}

void log_callback(void * /* avcl */, int level, const char *fmt, va_list args) {
    if (level > AV_LOG_VERBOSE) {
        return;
    }

    __android_log_vprint(to_android_log_level(level), LOG_PREFIX "ffmpeg", fmt,
                         args);
}

JNIEXPORT jint JNI_OnLoad(JavaVM *jvm, void * /* reserved */) {
    g_jvm = jvm;

    av_log_set_callback(log_callback);

    const AVCodec *codec = nullptr;
    void *it = nullptr;

    LOG_INFO("Available ffmpeg codecs:");
    while ((codec = av_codec_iterate(&it))) {
        LOG_INFO("- %s (%s)", codec->name, codec->long_name);
    }

    return JNI_VERSION_1_6;
}
