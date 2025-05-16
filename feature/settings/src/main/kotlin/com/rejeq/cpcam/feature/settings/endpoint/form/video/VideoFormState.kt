package com.rejeq.cpcam.feature.settings.endpoint.form.video

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.input.TextFieldValue
import com.rejeq.cpcam.core.data.model.PixFmt
import com.rejeq.cpcam.core.data.model.Resolution
import com.rejeq.cpcam.core.data.model.VideoCodec
import com.rejeq.cpcam.core.data.model.VideoConfig

@Immutable
data class VideoConfigFormState(
    val codecName: VideoCodec?,
    val pixFmt: PixFmt?,
    val bitrate: TextFieldValue,
    val framerate: TextFieldValue,
    val resolution: Pair<TextFieldValue, TextFieldValue>,
) {
    fun toDomain(): VideoConfig = VideoConfig(
        codecName = codecName,
        pixFmt = pixFmt,
        bitrate = bitrate.text.toIntOrNull(),
        framerate = framerate.text.toIntOrNull(),
        resolution = Resolution(
            width = resolution.first.text.toIntOrNull() ?: 0,
            height = resolution.second.text.toIntOrNull() ?: 0,
        ),
    )
}

fun VideoConfig.fromDomain() = VideoConfigFormState(
    codecName = codecName,
    pixFmt = pixFmt,
    bitrate = TextFieldValue(bitrate?.toString() ?: ""),
    framerate = TextFieldValue(framerate?.toString() ?: ""),
    resolution = Pair(
        TextFieldValue(resolution?.width?.toString() ?: ""),
        TextFieldValue(resolution?.height?.toString() ?: ""),
    ),
)
