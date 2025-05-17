#include "FFmpegOutput.h"

#include <vector>

#include "../JniUtils.h"
#include "../VideoConfig.h"

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_rejeq_cpcam_core_stream_jni_FFmpegOutputJni_create(
    JNIEnv *env, jobject /* obj */, jstring rawUrl, jstring rawProtocol) {
    FFmpegOutput *output = nullptr;
    std::string url = to_string(env, rawUrl);

    if (rawProtocol != nullptr) {
        std::string protocol = to_string(env, rawProtocol);
        output = FFmpegOutput::build(std::move(url), &protocol);
    } else {
        output = FFmpegOutput::build(std::move(url), nullptr);
    }

    return (jlong)output;
}

JNIEXPORT void JNICALL
Java_com_rejeq_cpcam_core_stream_jni_FFmpegOutputJni_destroy(JNIEnv * /* env */,
                                                             jobject /* obj */,
                                                             jlong output) {
    delete (FFmpegOutput *)output;
}

JNIEXPORT jint JNICALL
Java_com_rejeq_cpcam_core_stream_jni_FFmpegOutputJni_open(JNIEnv * /* env */,
                                                          jobject /* obj */,
                                                          jlong output) {
    return (int)((FFmpegOutput *)output)->open();
}

JNIEXPORT jint JNICALL
Java_com_rejeq_cpcam_core_stream_jni_FFmpegOutputJni_close(JNIEnv * /* env */,
                                                           jobject /* obj */,
                                                           jlong output) {
    return (int)((FFmpegOutput *)output)->close();
}

JNIEXPORT jlong JNICALL
Java_com_rejeq_cpcam_core_stream_jni_FFmpegOutputJni_makeVideoStream(
    JNIEnv *env, jobject /* obj */, jlong output, jobject rawConfig) {
    VideoConfig config = VideoConfig::build(env, rawConfig);
    auto *stream = ((FFmpegOutput *)output)->make_video_stream(config);
    return (jlong)stream;
}

JNIEXPORT jintArray JNICALL
Java_com_rejeq_cpcam_core_stream_jni_FFmpegOutputJniKt_nGetSupportedFormats(
    JNIEnv *env, jclass /* clazz */, jstring codec) {
    auto supported_fmts =
        FFmpegOutput::get_supported_formats(to_string(env, codec));

    if (supported_fmts.empty()) {
        return env->NewIntArray(0);
    }

    jintArray dst_arr = env->NewIntArray((jint)supported_fmts.size());
    if (dst_arr == nullptr) {
        return nullptr;
    }

    env->SetIntArrayRegion(dst_arr, 0, (jint)supported_fmts.size(),
                           (const jint *)supported_fmts.data());
    return dst_arr;
}
}
