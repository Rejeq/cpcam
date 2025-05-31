package com.rejeq.cpcam.feature.settings.endpoint.form.video

import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import com.rejeq.cpcam.core.data.model.PixFmt
import com.rejeq.cpcam.core.data.model.Resolution
import com.rejeq.cpcam.core.data.model.VideoCodec
import com.rejeq.cpcam.core.data.model.VideoConfig
import com.rejeq.cpcam.feature.settings.endpoint.form.field.EnumFieldState
import com.rejeq.cpcam.feature.settings.endpoint.form.field.IntegerFieldState
import com.rejeq.cpcam.feature.settings.endpoint.form.field.ResolutionFieldState

@Stable
class VideoFormState(
    initBitrate: Int?,
    initFramerate: Int?,
    initResolution: Resolution?,
    initCodec: VideoCodec? = null,
    initAvailableCodecs: List<VideoCodec> = emptyList(),
    initPixFmt: PixFmt? = null,
    initAvailablePixFmts: List<PixFmt> = emptyList(),
    getAvailablePixFmts: (VideoCodec) -> List<PixFmt>,
) {
    val codec = EnumFieldState<VideoCodec?>(
        initSelected = initCodec,
        initAvailables = initAvailableCodecs,
        onSelectedChange = {
            it?.let { codecState ->
                val availables = getAvailablePixFmts(codecState)
                pixFmt.onAvailablesChange(availables)
            }
        },
    )

    val pixFmt = EnumFieldState<PixFmt?>(
        initSelected = initPixFmt,
        initAvailables = initAvailablePixFmts,
    )

    val bitrate = IntegerFieldState(initBitrate)

    val framerate = IntegerFieldState(initFramerate)

    val resolution = ResolutionFieldState(initResolution)

    val state by derivedStateOf {
        VideoFormData(
            codecName = codec.state,
            pixFmt = pixFmt.state,
            bitrate = bitrate.state,
            framerate = framerate.state,
            resolution = resolution.state,
        )
    }
}

data class VideoFormData(
    val codecName: VideoCodec?,
    val pixFmt: PixFmt?,
    val bitrate: Int?,
    val framerate: Int?,
    val resolution: Resolution?,
) {
    fun toDomain(): VideoConfig = VideoConfig(
        codecName = codecName,
        pixFmt = pixFmt,
        bitrate = bitrate,
        framerate = framerate,
        resolution = resolution,
    )
}

fun VideoConfig.fromDomain(
    codecs: List<VideoCodec>,
    formats: List<PixFmt>,
    getAvailablePixFmts: (VideoCodec) -> List<PixFmt>,
) = VideoFormState(
    initAvailableCodecs = codecs,
    initCodec = codecName,
    initAvailablePixFmts = formats,
    initPixFmt = pixFmt,
    initBitrate = bitrate,
    initFramerate = framerate,
    initResolution = resolution,
    getAvailablePixFmts = getAvailablePixFmts,
)
