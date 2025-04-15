#include "FFmpegVideoStream.h"

#include <cassert>

extern "C" {
#include <libavutil/pixdesc.h>
#include <libswscale/swscale.h>
}

#include "FFmpegUtils.h"

#undef LOG_TAG
#define LOG_TAG "FFmpegVideoStream"
#include "Log.h"

FFmpegVideoStream::~FFmpegVideoStream() {
    av_frame_free(&m_sws_frame);
    sws_freeContext(m_sws_ctx);

    av_frame_free(&m_frame);
    av_packet_free(&m_packet);

    avcodec_free_context(&m_cctx);
}

FFmpegVideoStream *FFmpegVideoStream::build(AVFormatContext *octx,
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

    return new FFmpegVideoStream(octx, cctx, packet, frame);
}

void FFmpegVideoStream::send_frame(const FrameData &data) {
    if (!m_is_started) {
        LOG_TRACE("Stream is not started, does nothing");
        return;
    }

    if (m_frame_width != data.width && m_frame_height != data.height) {
        LOG_WARN("Frame size was incorrect: Updating frame size to (%d, %d)",
                 data.width, data.height);

        set_frame_size(data.width, data.height);
    }

    std::lock_guard<std::mutex> lock(m_sending_lock);

    as_av_frame(data, m_frame);

    if (!m_is_sws_required) {
        write_to_encoder(m_frame);
    } else {
        make_sws_scale(m_frame, m_sws_frame);
        write_to_encoder(m_sws_frame);
    }
}

void FFmpegVideoStream::set_pixel_format(PixFmt pix_fmt) {
    std::lock_guard<std::mutex> lock(m_sending_lock);

    m_pix_fmt = to_av_pix_fmt(pix_fmt);

    m_pix_fmt_plane_count = av_pix_fmt_count_planes(m_pix_fmt);
    if (m_pix_fmt == m_cctx->pix_fmt) {
        return;
    }

    LOG_WARN("Codec pix_fmt doesn't match with source pix_fmt, using sws");
    require_sws();
}

void FFmpegVideoStream::set_frame_size(int width, int height) {
    std::lock_guard<std::mutex> lock(m_sending_lock);

    if (m_cctx->width != width || m_cctx->height != height) {
        LOG_WARN("Codec size not match with source size: (%d; %d) != (%d; %d)",
                 m_cctx->width, m_cctx->height, width, height);

        require_sws();
    }

    m_frame_width = width;
    m_frame_height = height;
}

void FFmpegVideoStream::start() {
    std::lock_guard<std::mutex> lock(m_sending_lock);
    m_is_started = true;
}

void FFmpegVideoStream::stop() {
    std::lock_guard<std::mutex> lock(m_sending_lock);
    m_is_started = false;
}

void FFmpegVideoStream::write_to_encoder(AVFrame *frame) {
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

void FFmpegVideoStream::as_av_frame(const FrameData &data, AVFrame *out) {
    assert(data.width > 0 && data.height > 0);
    out->width = data.width;
    out->height = data.height;
    out->format = m_pix_fmt;

    assert(m_pix_fmt_plane_count <= AV_NUM_DATA_POINTERS);
    for (int i = 0; i < m_pix_fmt_plane_count; i++) {
        // all planes for current pix_fmt must be valid
        assert(data.buff[i] != nullptr);
        // assert(data.buff_stride[i] > 0);

        out->data[i] = data.buff[i];
        out->linesize[i] = data.buff_stride[i];
    }

    out->pts = data.ts - m_octx->start_time;
}

void FFmpegVideoStream::make_sws_scale(AVFrame *input, AVFrame *output) {
    assert(m_is_sws_required == true);

    if (av_frame_make_writable(output) < 0) {
        LOG_ERROR("Unable to make frame writeable");
        return;
    }

    if (m_is_sws_invalid) {
        sws_freeContext(m_sws_ctx);
        m_sws_ctx = nullptr;
        m_is_sws_invalid = false;
    }

    // NOTE: Allocation placed here, because actual pixel format can be
    // determined right before send_frame
    if (!m_sws_ctx) {
        m_sws_ctx = sws_getContext(
            input->width, input->height, (AVPixelFormat)input->format,
            output->width, output->height, (AVPixelFormat)output->format,
            SWS_FAST_BILINEAR, nullptr, nullptr, nullptr);
    }

    if (!m_sws_ctx) {
        LOG_ERROR("Unable to initialize the sws context");
        return;
    }

    sws_scale(m_sws_ctx, input->data, input->linesize, 0, input->height,
              output->data, output->linesize);

    av_frame_copy_props(output, input);
}

void FFmpegVideoStream::require_sws() {
    // FIXME: Data leak when called multiple times
    m_sws_frame = make_av_frame(m_cctx->width, m_cctx->height, m_cctx->pix_fmt);

    if (m_sws_ctx) {
        m_is_sws_invalid = true;
    }

    m_is_sws_required = true;
}
