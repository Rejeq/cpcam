#pragma once

#include <mutex>

extern "C" {
#include "libavcodec/avcodec.h"
#include "libavcodec/packet.h"
#include "libavformat/avformat.h"
#include "libavutil/frame.h"
}

#include "FrameData.h"

class FFmpegVideoStream {
   public:
    FFmpegVideoStream(AVFormatContext *octx, AVCodecContext *cctx,
                      AVPacket *packet, AVFrame *frame)
        : m_octx(octx), m_cctx(cctx), m_packet(packet), m_frame(frame) {}
    ~FFmpegVideoStream();

    static FFmpegVideoStream *build(AVFormatContext *octx,
                                    AVCodecContext *cctx);

    void send_frame(const FrameData &data);
    void set_pixel_format(PixFmt pix_fmt);
    void set_frame_size(int width, int height);

    bool has_pixel_format() const { return m_pix_fmt != AV_PIX_FMT_NONE; }
    AVPixelFormat pixel_format() const { return m_pix_fmt; }
    int plane_count() const { return m_pix_fmt_plane_count; }

    void start();
    void stop();

    // TODO:
    // Looks like it's good idea to create object that can control optimal pixel
    // formats for sources and streams. Source and Stream must provide
    // get_preffered_formats() that returns list of supported formats without
    // using conversion.
    // std::vector<PixFmt> FFmpegVideoStream::get_preferred_formats() {
    //     return {
    //         PixFmt::YUV420P,
    //         PixFmt::NV12,
    //     };
    // }

   private:
    void write_to_encoder(AVFrame *frame);

    // Represent FrameData as AVFrame. In this case AVFrame is not refcounted
    // and considered as read-only
    void as_av_frame(const FrameData &data, AVFrame *out);

    void make_sws_scale(AVFrame *input, AVFrame *output);
    void require_sws();

    std::mutex m_sending_lock;

    AVFormatContext *m_octx;
    AVCodecContext *m_cctx;

    AVPacket *m_packet;
    AVFrame *m_frame;

    struct SwsContext *m_sws_ctx = nullptr;
    AVFrame *m_sws_frame = nullptr;

    int64_t m_start_pts = -1;

    AVPixelFormat m_pix_fmt = AV_PIX_FMT_NONE;
    int m_frame_width = 0;
    int m_frame_height = 0;
    int m_pix_fmt_plane_count = 0;
    bool m_is_sws_required = false;
    bool m_is_sws_invalid = false;
    bool m_is_started = false;
};
