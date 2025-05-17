#pragma once

#include <array>

extern "C" {
#include "libavcodec/packet.h"
#include "libavformat/avformat.h"
#include <libavutil/error.h>
#include <libavutil/frame.h>
#include <libavutil/pixfmt.h>
#include <libavutil/timestamp.h>
}

#include "PixFmt.h"

// Returns std::array since it uses stack allocation without copying
std::array<char, AV_ERROR_MAX_STRING_SIZE> av_err_to_string(int err);

// Returns std::array since it uses stack allocation without copying
std::array<char, AV_TS_MAX_STRING_SIZE> av_ts_to_string(int64_t err);

// Returns std::array since it uses stack allocation without copying
std::array<char, AV_TS_MAX_STRING_SIZE> av_ts_to_time_string(
    int64_t ts, AVRational *time_base);

#define LOG_PACKET_INFO(octx, pkt)                                            \
    do {                                                                      \
        AVRational *time_base = &octx->streams[pkt->stream_index]->time_base; \
                                                                              \
        LOG_TRACE(                                                            \
            "pts:%s "                                                         \
            "pts_time:%s "                                                    \
            "dts:%s "                                                         \
            "dts_time:%s "                                                    \
            "duration:%s "                                                    \
            "duration_time:%s "                                               \
            "stream_index:%d ",                                               \
            av_ts_to_string(pkt->pts).data(),                                 \
            av_ts_to_time_string(pkt->pts, time_base).data(),                 \
            av_ts_to_string(pkt->dts).data(),                                 \
            av_ts_to_time_string(pkt->dts, time_base).data(),                 \
            av_ts_to_string(pkt->duration).data(),                            \
            av_ts_to_time_string(pkt->duration, time_base).data(),            \
            pkt->stream_index);                                               \
    } while (0)

AVPixelFormat to_av_pix_fmt(PixFmt pix_fmt);
PixFmt from_av_pix_fmt(AVPixelFormat pix_fmt);

AVFrame *make_av_frame(int width, int height, int pix_fmt);
