#include "FFmpegOutput.h"

#include <cassert>

extern "C" {
#include <libavcodec/avcodec.h>
}

#include "../FFmpegUtils.h"

#define LOG_TAG "FFmpegOutput"
#include "../Log.h"

FFmpegOutput::~FFmpegOutput() {
    avformat_free_context(m_octx);
}

FFmpegOutput *FFmpegOutput::build(std::string url,
                                  const std::string *protocol) {
    AVFormatContext *octx = nullptr;
    int res = 0;

    const char *protocol_str = (protocol) ? protocol->c_str() : nullptr;
    res = avformat_alloc_output_context2(&octx, nullptr, protocol_str,
                                         url.c_str());
    if (!octx || res < 0) {
        LOG_ERROR("Unable to open output context: %s",
                  av_err_to_string(res).data());
        return nullptr;
    }

    auto *output = new FFmpegOutput(std::move(url), octx);

    return output;
}

bool FFmpegOutput::open() {
    LOG_INFO("Opening");

    if (m_is_open) {
        LOG_WARN("Unable to open FFmpegOutput: Already opened");
        return true;
    }

    const char *url = m_url.c_str();

    // TODO: Dump info about all streams
    av_dump_format(m_octx, 0, url, 1);

    const AVOutputFormat *fmt = m_octx->oformat;
    int res = 0;

    if (!(fmt->flags & AVFMT_NOFILE)) {
        res = avio_open(&m_octx->pb, url, AVIO_FLAG_WRITE);
        if (res < 0) {
            LOG_ERROR("Unable to open '%s' url: %s\n", url,
                      av_err_to_string(res).data());
            return true;
        }
    }

    res = avformat_write_header(m_octx, nullptr);
    if (res < 0) {
        LOG_ERROR("Unable to write header: %s", av_err_to_string(res).data());
        return true;
    }

    m_is_open = true;
    return false;
}

bool FFmpegOutput::close() {
    if (!m_is_open) {
        LOG_WARN("Unable to close: Not opened");
        return true;
    }

    const AVOutputFormat *fmt = m_octx->oformat;

    // TODO: Add check that ensures that all streams are closed

    LOG_INFO("Writing trailer");
    av_write_trailer(m_octx);

    if (!(fmt->flags & AVFMT_NOFILE)) {
        /* Close the output file. */
        avio_closep(&m_octx->pb);
    }

    m_is_open = false;
    return false;
}

FFmpegVideoStream *FFmpegOutput::make_video_stream(const VideoConfig &config) {
    // TODO: Fail if output already started

    assert((int)m_octx->nb_streams < m_octx->max_streams);
    AVStream *st = avformat_new_stream(m_octx, nullptr);
    if (!st) {
        LOG_ERROR("Could not allocate stream\n");
        return nullptr;
    }

    // NOTE: Id can be changed internally by ffmpeg
    st->id = st->index;

    const char *codec_name = config.codec_name.c_str();
    const AVCodec *codec = avcodec_find_encoder_by_name(codec_name);
    if (!codec) {
        LOG_ERROR("Unable to find '%s' encoder", codec_name);
        return nullptr;
    }

    AVCodecContext *cctx = avcodec_alloc_context3(codec);
    if (!cctx) {
        LOG_ERROR("Unable to allocate codec context\n");
        return nullptr;
    }

    cctx->codec_id = codec->id;
    cctx->bit_rate = config.bitrate;
    cctx->width = config.width;
    cctx->height = config.height;
    st->time_base = {1, 1'000'000'000};
    cctx->time_base = {1, 1'000'000'000};
    //     cctx->time_base = {1, config.framerate};
    cctx->framerate = {config.framerate, 1};
    cctx->pix_fmt = to_av_pix_fmt(config.pix_fmt);

    if (m_octx->oformat->flags & AVFMT_GLOBALHEADER) {
        cctx->flags |= AV_CODEC_FLAG_GLOBAL_HEADER;
    }

    AVDictionary *options = nullptr;
    av_dict_set(&options, "preset", "veryslow", 0);

    int res = avcodec_open2(cctx, codec, &options);
    if (res < 0) {
        LOG_ERROR("Unable to open video codec: %s",
                  av_err_to_string(res).data());
        avcodec_free_context(&cctx);
        return nullptr;
    }

    res = avcodec_parameters_from_context(st->codecpar, cctx);
    if (res < 0) {
        LOG_ERROR("Could not copy the stream parameters");
        avcodec_free_context(&cctx);
        return nullptr;
    }

    auto *stream = FFmpegVideoStream::build(m_octx, cctx);
    if (!stream) {
        LOG_ERROR("Unable to allocate video stream");
        avcodec_free_context(&cctx);
        return nullptr;
    }

    return stream;
}
