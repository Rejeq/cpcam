#pragma once

extern "C" {
#include "libavcodec/avcodec.h"
#include "libavcodec/packet.h"
#include "libavformat/avformat.h"
#include "libavutil/frame.h"
}

#include "StreamData.h"

class FFmpegAudioStream {
    public:
    FFmpegAudioStream(AVFormatContext *octx, AVCodecContext *cctx,
            AVPacket *packet, AVFrame *frame)
    : m_octx(octx), m_cctx(cctx), m_packet(packet), m_frame(frame) {}
    ~FFmpegAudioStream();


    static FFmpegAudioStream *build(AVFormatContext *octx,
                                    AVCodecContext *cctx);

    void send(const AudioData &data);
    void set_format(SampleFormat format);

    bool has_format() const { return m_format != AV_SAMPLE_FMT_NONE; }
    AVSampleFormat format() const { return m_format; }
    int plane_count() const { return m_pix_fmt_plane_count; }

    void start();
    void stop();

   private:
    void as_av_frame(const AudioData &data, AVFrame *out, int offset);
    void write_to_encoder(AVFrame *frame);

    AVFormatContext *m_octx;
    AVCodecContext *m_cctx;

    AVPacket *m_packet;
    AVFrame *m_frame;


    struct SwrContext *m_swr_ctx = nullptr;
    AVFrame *m_swr_frame = nullptr;

    AVSampleFormat m_format = AV_SAMPLE_FMT_NONE;
    int m_pix_fmt_plane_count = 0;
    bool m_is_sws_required = false;
    bool m_is_sws_invalid = false;
    bool m_is_started = false;
};
