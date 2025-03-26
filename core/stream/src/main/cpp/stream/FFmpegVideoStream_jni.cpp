#include <cassert>
#include <jni.h>
#include <media/NdkImage.h>
#include <optional>

#define LOG_TAG "VideoStream"
#include "Log.h"
#include "stream/FFmpegVideoStream.h"

std::optional<PixFmt> get_image_format(const FrameData &data, int format,
                                       const int *pixel_stride) {
    if (format != AIMAGE_FORMAT_YUV_420_888) {
        // TODO: Add support for other formats
        return std::nullopt;
    }

    if (pixel_stride[1] != pixel_stride[2]) {
        LOG_ERROR(
            "Unable to get image format: "
            "Pixel strides of U and V plane should have been the same");
        return std::nullopt;
    }

    if (pixel_stride[1] == 1) {
        return std::make_optional(PixFmt::YUV420P);
    }

    if (pixel_stride[1] == 2) {
        auto fmt = (data.buff[1] < data.buff[2]) ? PixFmt::NV12 : PixFmt::NV21;
        return std::make_optional(fmt);
    }

    LOG_WARN(
        "Unable to get image format: "
        "Unknown pixel stride '%d' of U and V plane",
        pixel_stride[1]);

    return std::nullopt;
}

extern "C" {

JNIEXPORT void JNICALL
Java_com_rejeq_cpcam_core_stream_jni_FFmpegVideoStreamJni_send(
    JNIEnv *env, jobject /* obj */, jlong rawStream, jlong ts, jint width,
    jint height, jint format, jobjectArray buffers, jintArray strides,
    jintArray pixelStrides) {
    size_t buff_len = env->GetArrayLength(buffers);
    size_t stride_len = env->GetArrayLength(strides);
    size_t pixel_stride_len = env->GetArrayLength(pixelStrides);
    assert(buff_len == stride_len && buff_len == pixel_stride_len);

    auto data = FrameData{
        .ts = ts,
        .width = width,
        .height = height,
        .buff = {},
        .buff_stride = {},
    };

    int *stride_ptr = env->GetIntArrayElements(strides, nullptr);

    int len = (int)std::min(buff_len, sizeof(data.buff) / sizeof(*data.buff));
    for (int idx = 0; idx < len; idx++) {
        jobject buffer = env->GetObjectArrayElement(buffers, idx);

        data.buff[idx] = (uint8_t *)env->GetDirectBufferAddress(buffer);
        data.buff_stride[idx] = stride_ptr[idx];
    }

    // TODO: Move deducing actual pixel format to kotlin side
    auto *stream = (FFmpegVideoStream *)rawStream;
    if (!stream->has_pixel_format()) {
        int *pixel_stride_ptr = env->GetIntArrayElements(pixelStrides, nullptr);

        auto pix_fmt = get_image_format(data, format, pixel_stride_ptr);
        if (!pix_fmt.has_value()) {
            LOG_ERROR("Unable to get image format");
            return;
        }

        stream->set_pixel_format(*pix_fmt);

        env->ReleaseIntArrayElements(pixelStrides, pixel_stride_ptr, JNI_ABORT);
    }

    jobject buffer = env->GetObjectArrayElement(buffers, 0);

    data.buff[0] = (uint8_t *)env->GetDirectBufferAddress(buffer);
    data.buff_stride[0] = stride_ptr[0];

    switch (stream->pixel_format()) {
        case AV_PIX_FMT_YUV420P:
            buffer = env->GetObjectArrayElement(buffers, 1);

            data.buff[1] = (uint8_t *)env->GetDirectBufferAddress(buffer);
            data.buff_stride[1] = stride_ptr[1];

            buffer = env->GetObjectArrayElement(buffers, 2);

            data.buff[2] = (uint8_t *)env->GetDirectBufferAddress(buffer);
            data.buff_stride[2] = stride_ptr[2];

            break;
        case AV_PIX_FMT_NV12:
            buffer = env->GetObjectArrayElement(buffers, 1);

            data.buff[1] = (uint8_t *)env->GetDirectBufferAddress(buffer);
            data.buff_stride[1] = stride_ptr[1];

        case AV_PIX_FMT_NV21:
            buffer = env->GetObjectArrayElement(buffers, 2);

            data.buff[1] = (uint8_t *)env->GetDirectBufferAddress(buffer);
            data.buff_stride[1] = stride_ptr[2];
            break;
        default:
            LOG_ERROR("Stream has unknown pixel format: %d", stream->pixel_format());
            return;
    }

    env->ReleaseIntArrayElements(strides, stride_ptr, JNI_ABORT);

    stream->send_frame(data);
}

JNIEXPORT void JNICALL
Java_com_rejeq_cpcam_core_stream_jni_FFmpegVideoStreamJni_setResolution(
    JNIEnv * /* env */, jobject /* obj */, jlong rawStream, jint width,
    jint height) {
    auto *stream = (FFmpegVideoStream *)rawStream;

    stream->set_frame_size(width, height);
}

JNIEXPORT void JNICALL
Java_com_rejeq_cpcam_core_stream_jni_FFmpegVideoStreamJni_start(
    JNIEnv * /* env */, jobject /* obj */, jlong rawStream) {
    auto *stream = (FFmpegVideoStream *)rawStream;

    stream->start();
}

JNIEXPORT void JNICALL
Java_com_rejeq_cpcam_core_stream_jni_FFmpegVideoStreamJni_stop(
    JNIEnv * /* env */, jobject /* obj */, jlong rawStream) {
    auto *stream = (FFmpegVideoStream *)rawStream;

    stream->stop();
}
}
