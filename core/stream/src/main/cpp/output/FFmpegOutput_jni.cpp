#include "FFmpegOutput.h"

#include "../JniUtils.h"
#include "../StreamConfig.h"

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

JNIEXPORT jboolean JNICALL
Java_com_rejeq_cpcam_core_stream_jni_FFmpegOutputJni_open(JNIEnv * /* env */,
                                                          jobject /* obj */,
                                                          jlong output) {
    return ((FFmpegOutput *)output)->open();
}

JNIEXPORT jboolean JNICALL
Java_com_rejeq_cpcam_core_stream_jni_FFmpegOutputJni_close(JNIEnv * /* env */,
                                                           jobject /* obj */,
                                                           jlong output) {
    return ((FFmpegOutput *)output)->close();
}

JNIEXPORT jlong JNICALL
Java_com_rejeq_cpcam_core_stream_jni_FFmpegOutputJni_makeVideoStream(
    JNIEnv *env, jobject /* obj */, jlong output, jobject rawConfig) {
    VideoConfig config = VideoConfig::build(env, rawConfig);
    auto *stream = ((FFmpegOutput *)output)->make_video_stream(config);
    return (jlong)stream;
}

JNIEXPORT jlong JNICALL
Java_com_rejeq_cpcam_core_stream_jni_FFmpegOutputJni_makeAudioStream(
        JNIEnv *env, jobject /* obj */, jlong output, jobject rawConfig) {
    AudioConfig config = AudioConfig::build(env, rawConfig);
    auto *stream = ((FFmpegOutput *)output)->make_audio_stream(config);
    return (jlong)stream;
}

}
