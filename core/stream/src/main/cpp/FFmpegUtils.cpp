#include "FFmpegUtils.h"

#include <cassert>

#define LOG_TAG "FFmpegUtils"
#include "Log.h"

std::array<char, AV_ERROR_MAX_STRING_SIZE> av_err_to_string(int err) {
    std::array<char, AV_ERROR_MAX_STRING_SIZE> out = {};

    av_make_error_string(out.data(), AV_ERROR_MAX_STRING_SIZE, err);
    return out;
}

std::array<char, AV_TS_MAX_STRING_SIZE> av_ts_to_string(int64_t err) {
    std::array<char, AV_TS_MAX_STRING_SIZE> out = {};

    av_ts_make_string(out.data(), err);
    return out;
}

std::array<char, AV_TS_MAX_STRING_SIZE> av_ts_to_time_string(
    int64_t ts, AVRational *time_base) {
    std::array<char, AV_TS_MAX_STRING_SIZE> out = {};

    av_ts_make_time_string(out.data(), ts, time_base);
    return out;
}

AVPixelFormat to_av_pix_fmt(PixFmt pix_fmt) {
    switch (pix_fmt) {
        case PixFmt::Unknown: return AV_PIX_FMT_NONE;
        case PixFmt::YUV420P: return AV_PIX_FMT_YUV420P;
        case PixFmt::YUV444P: return AV_PIX_FMT_YUV444P;
        case PixFmt::NV12: return AV_PIX_FMT_NV12;
        case PixFmt::NV21: return AV_PIX_FMT_NV21;
        case PixFmt::RGBA: return AV_PIX_FMT_RGBA;
        case PixFmt::RGB24: return AV_PIX_FMT_RGB24;
    }

    assert(false && "Unknown pixel format");
}

PixFmt from_av_pix_fmt(AVPixelFormat pix_fmt) {
    switch (pix_fmt) {
        case AV_PIX_FMT_YUV420P: return PixFmt::YUV420P;
        case AV_PIX_FMT_YUV444P: return PixFmt::YUV444P;
        case AV_PIX_FMT_NV12: return PixFmt::NV12;
        case AV_PIX_FMT_NV21: return PixFmt::NV21;
        case AV_PIX_FMT_RGBA: return PixFmt::RGBA;
        case AV_PIX_FMT_RGB24: return PixFmt::RGB24;
        default: return PixFmt::Unknown;
    }
}

AVFrame *make_av_frame(int width, int height, int pix_fmt) {
    AVFrame *frame = av_frame_alloc();
    if (!frame) {
        LOG_ERROR("Unable to allocate temporary sws frame");
        return nullptr;
    }

    frame->format = pix_fmt;
    frame->width = width;
    frame->height = height;

    int ret = av_frame_get_buffer(frame, 0);
    if (ret < 0) {
        LOG_ERROR("Unable to allocate frame data");
        av_frame_free(&frame);
        return nullptr;
    }

    return frame;
}
