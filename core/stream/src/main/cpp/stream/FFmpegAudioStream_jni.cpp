#include <cassert>
#include <jni.h>
#include <media/NdkImage.h>
#include <optional>

#define LOG_TAG "FFmpegAudioStream"
#include "Log.h"
#include "stream/FFmpegAudioStream.h"

extern "C" {

JNIEXPORT void JNICALL
Java_com_rejeq_cpcam_core_stream_jni_FFmpegAudioStreamJni_send(
    JNIEnv *env, jobject /* obj */, jlong rawStream, jlong ts, jint samplesCount,
    jint sampleRate, jint channelCount, jint format, jobject buffer) {
    auto *stream = reinterpret_cast<FFmpegAudioStream *>(rawStream);
    if (!stream) {
        LOG_ERROR("Invalid stream pointer");
        return;
    }

    auto data = AudioData{
        .ts = ts,
        .samples_count = samplesCount,
        .sample_rate = sampleRate,
        .channel_count = channelCount,
        .buff = {},
        .buff_stride = {},
    };

    if (!stream->has_format()) {
        stream->set_format((SampleFormat) format);
    }

    auto* buff = (uint8_t*) env->GetDirectBufferAddress(buffer);
    auto capacity = env->GetDirectBufferCapacity(buffer);
    data.buff[0] = buff;
    data.buff_stride[0] = capacity;


    stream->send(data);
}

JNIEXPORT void JNICALL
Java_com_rejeq_cpcam_core_stream_jni_FFmpegAudioStreamJni_start(
    JNIEnv * /* env */, jobject /* obj */, jlong rawStream) {
    auto *stream = (FFmpegAudioStream *)rawStream;

    stream->start();
}

JNIEXPORT void JNICALL
Java_com_rejeq_cpcam_core_stream_jni_FFmpegAudioStreamJni_stop(
    JNIEnv * /* env */, jobject /* obj */, jlong rawStream) {
    auto *stream = (FFmpegAudioStream *)rawStream;

    stream->stop();
}
}
