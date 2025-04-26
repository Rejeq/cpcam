#include <cassert>
#include <jni.h>
#include <media/NdkImage.h>
#include <optional>

#define LOG_TAG "VideoStream"
#include "Log.h"
#include "stream/FFmpegVideoStream.h"

std::optional<PixFmt> get_image_format(const FrameData &data, int format,
                                       const int *pixel_stride) {
    assert(pixel_stride);

    if (format != AIMAGE_FORMAT_YUV_420_888) {
        LOG_ERROR("Unsupported image format: %d", format);
        return std::nullopt;
    }

    if (pixel_stride[1] != pixel_stride[2]) {
        LOG_ERROR("U and V plane pixel strides must be equal: U=%d, V=%d",
                  pixel_stride[1], pixel_stride[2]);
        return std::nullopt;
    }

    switch (pixel_stride[1]) {
        case 1: return std::make_optional(PixFmt::YUV420P);
        case 2: {
            auto fmt = (data.buff[1] < data.buff[2]) ? PixFmt::NV12
                                                     : PixFmt::NV21;
            return std::make_optional(fmt);
        }
        default:
            LOG_WARN("Unknown pixel stride for U/V planes: %d",
                     pixel_stride[1]);
            return std::nullopt;
    }
}

bool setupFrameData(FrameData &data, JNIEnv *env, jobjectArray buffers,
                    jintArray strides, int plane_count) {
    assert(plane_count > 0);
    assert(plane_count <= (int)(sizeof(data.buff) / sizeof(*data.buff)));

    const jsize buffer_len = env->GetArrayLength(buffers);
    const jsize stride_len = env->GetArrayLength(strides);

    if (buffer_len < plane_count || stride_len < plane_count) {
        LOG_ERROR(
            "Buffer length (%d) or stride length (%d) less than required plane "
            "count (%d)",
            buffer_len, stride_len, plane_count);
        return false;
    }

    int *stride_ptr = env->GetIntArrayElements(strides, nullptr);
    if (!stride_ptr) {
        LOG_ERROR("Failed to get stride array");
        return false;
    }

    bool failure = false;
    for (int i = 0; i < plane_count; i++) {
        jobject buffer = env->GetObjectArrayElement(buffers, i);
        if (!buffer) {
            LOG_ERROR("Failed to get buffer at index %d", i);
            failure = true;
            break;
        }

        auto *plane_data = (uint8_t *)env->GetDirectBufferAddress(buffer);
        if (!plane_data) {
            LOG_ERROR("Failed to get direct buffer address at index %d", i);
            failure = true;
            break;
        }

        data.buff[i] = plane_data;
        data.buff_stride[i] = stride_ptr[i];
    }

    env->ReleaseIntArrayElements(strides, stride_ptr, JNI_ABORT);
    return failure;
}

extern "C" {

JNIEXPORT void JNICALL
Java_com_rejeq_cpcam_core_stream_jni_FFmpegVideoStreamJni_send(
    JNIEnv *env, jobject /* obj */, jlong rawStream, jlong ts, jint width,
    jint height, jint format, jint planeCount, jobjectArray buffers,
    jintArray strides, jintArray pixelStrides) {
    auto *stream = reinterpret_cast<FFmpegVideoStream *>(rawStream);
    if (!stream) {
        LOG_ERROR("Invalid stream pointer");
        return;
    }

    auto data = FrameData{
        .ts = ts,
        .width = width,
        .height = height,
        .buff = {},
        .buff_stride = {},
    };

    if (setupFrameData(data, env, buffers, strides, planeCount)) {
        return;
    }

    if (!stream->has_pixel_format()) {
        int *pixel_strides = env->GetIntArrayElements(pixelStrides, nullptr);
        if (!pixel_strides) {
            LOG_ERROR("Failed to get pixel stride array");
            return;
        }

        auto pix_fmt = get_image_format(data, format, pixel_strides);
        env->ReleaseIntArrayElements(pixelStrides, pixel_strides, JNI_ABORT);

        if (!pix_fmt) {
            LOG_ERROR("Unable to get image format");
            return;
        }

        stream->set_pixel_format(*pix_fmt);
    }

    auto stream_fmt = stream->pixel_format();
    if (stream_fmt == AV_PIX_FMT_NV21) {
        // NV12/NV21 has only 2 planes (Y and U/V)
        // In case of NV21 we need to set V plane as second
        // In case of NV12 we already have correct order
        data.buff[1] = data.buff[2];
        data.buff_stride[1] = data.buff_stride[2];
    }

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
