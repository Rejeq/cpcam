#pragma once

#include <string>

extern "C" {
#include <libavformat/avformat.h>
}

#include "StreamConfig.h"
#include "stream/FFmpegAudioStream.h"
#include "stream/FFmpegVideoStream.h"

// TODO: Better error handling
class FFmpegOutput {
   public:
    FFmpegOutput(std::string url, AVFormatContext *octx)
        : m_url(std::move(url)), m_octx(octx) {}
    ~FFmpegOutput();

    static FFmpegOutput *build(std::string url,
                               const std::string *protocol = nullptr);

    bool open();
    bool close();

    FFmpegVideoStream *make_video_stream(const VideoConfig &config);
    FFmpegAudioStream *make_audio_stream(const AudioConfig &config);

   private:
    // TODO: AVFormatContext has url field, consider using it
    std::string m_url;

    AVFormatContext *m_octx;
    bool m_is_open = false;
};
