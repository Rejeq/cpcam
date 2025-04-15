#include "FFmpegAudioStream.h"

#include <cassert>

extern "C" {
#include <libavutil/pixdesc.h>
#include <libswresample/swresample.h>
}

#include "FFmpegUtils.h"

#define LOG_TAG "FFmpegAudioStream"
#include "Log.h"

FFmpegAudioStream::~FFmpegAudioStream() {
    av_frame_free(&m_swr_frame);
    swr_free(&m_swr_ctx);

    av_frame_free(&m_frame);
    av_packet_free(&m_packet);

    avcodec_free_context(&m_cctx);
}

FFmpegAudioStream *FFmpegAudioStream::build(AVFormatContext *octx,
                                            AVCodecContext *cctx) {
    LOG_DEBUG("Building stream with size: (%d, %d)", cctx->width, cctx->height);

    AVPacket *packet = av_packet_alloc();
    if (!packet) {
        LOG_ERROR("Unable to allocate packet");
        return nullptr;
    }

    AVFrame *frame = av_frame_alloc();
    if (!frame) {
        LOG_ERROR("Unable to allocate frame");
        av_packet_free(&packet);
        return nullptr;
    }

    frame->pts = 0;

    return new FFmpegAudioStream(octx, cctx, packet, frame);
}

void FFmpegAudioStream::send(const AudioData &data) {
    if (!m_is_started) {
        LOG_TRACE("Stream is not started, does nothing");
        return;
    }

    int samples = data.samples_count;
    while (samples >= 1024) {
//    while (samples >= (data.samples_count - 1024 * 8)) {
        AVFrame *frame = av_frame_alloc();
        if (!frame) {
            LOG_ERROR("Unable to allocate frame");
            abort();
        }

        frame->pts = m_frame->pts - 1024;

        as_av_frame(data, frame, data.samples_count - samples);

        write_to_encoder(frame);
        samples -= 1024;

        av_frame_free(&frame);
        m_frame->pts += 1024;
    }
}

void FFmpegAudioStream::set_format(SampleFormat format) {
    LOG_INFO("Setting format to: %d", (int)format);

    m_format = to_av_sample_fmt(format);
}

void FFmpegAudioStream::start() {
    m_is_started = true;
}

void FFmpegAudioStream::stop() {
    m_is_started = false;
}

void FFmpegAudioStream::as_av_frame(const AudioData &data, AVFrame *out, int offset) {
    assert(data.channel_count == 1);
    AVChannelLayout tmp = AV_CHANNEL_LAYOUT_MONO;
    av_channel_layout_copy(&out->ch_layout, &tmp);

//    out->nb_samples = data.samples_count;
    out->nb_samples = 1024;
    out->sample_rate = 44100;
    out->format = m_format;

    for (int i = 0; i < 1; i++) {
        // all planes for current pix_fmt must be valid
        assert(data.buff[i] != nullptr);

        LOG_INFO("Some: %d", av_get_bytes_per_sample(m_cctx->sample_fmt));

        // TODO:
        // av_get_bytes_per_sample(codec_context->sample_fmt)
        out->data[i] = data.buff[i] + offset * sizeof(float);
//        out->linesize[i] = data.buff_stride[i];
        out->linesize[i] = 1024 * sizeof(float);
    }

//    static size_t some = 0;
    out->pts += 1024;
    out->pkt_dts += out->pts;
//    some += 10000 + offset;

//    out->pts = data.ts - m_octx->start_time;
//    out->pts = (data.ts + offset) - m_octx->start_time;
//    out->pkt_dts = out->pts;
}

void FFmpegAudioStream::write_to_encoder(AVFrame *frame) {
    LOG_TRACE("Writing frame to the encoder");
    bool wantAgain = false;

    do {
        wantAgain = false;

        int res = avcodec_send_frame(m_cctx, frame);
        if (res < 0) {
            if (res == AVERROR(EAGAIN)) {
                wantAgain = true;
            } else {
                LOG_ERROR("Error sending a frame to the encoder: %s(%d)",
                          av_err_to_string(res).data(), res);
                break;
            }
        }

        res = avcodec_receive_packet(m_cctx, m_packet);
        if (res < 0) {
            // EAGAIN cannot be returned from send_frame and receive_packet at
            // the same time.
            if (res != AVERROR(EAGAIN)) {
                LOG_ERROR("Error receive packet from the encoder: %s(%d)",
                          av_err_to_string(res).data(), res);
            }

            break;
        }

        LOG_PACKET_INFO(m_octx, m_packet);
        res = av_write_frame(m_octx, m_packet);
        av_packet_unref(m_packet);

        if (res < 0) {
            LOG_ERROR("Error while writing output packet: %s(%d)",
                      av_err_to_string(res).data(), res);
            return;
        }
    } while (wantAgain);
}

//void FFmpegAudioStream::make_sw_resample(AVFrame *input, AVFrame *output) {
//    assert(m_is_sws_required == true);
//
//    if (av_frame_make_writable(output) < 0) {
//        LOG_ERROR("Unable to make frame writeable");
//        return;
//    }
//
//    if (m_is_sws_invalid) {
//        sws_freeContext(m_sws_ctx);
//        m_sws_ctx = nullptr;
//        m_is_sws_invalid = false;
//    }
//
//    // NOTE: Allocation placed here, because actual pixel format can be
//    // determined right before send_frame
//    if (!m_sws_ctx) {
//        m_sws_ctx = sws_getContext(
//                input->width, input->height, (AVPixelFormat)input->format,
//                output->width, output->height, (AVPixelFormat)output->format,
//                SWS_FAST_BILINEAR, nullptr, nullptr, nullptr);
//    }
//
//    if (!m_sws_ctx) {
//        LOG_ERROR("Unable to initialize the sws context");
//        return;
//    }
//
//    sws_scale(m_sws_ctx, input->data, input->linesize, 0, input->height,
//              output->data, output->linesize);
//
//    av_frame_copy_props(output, input);
//}

