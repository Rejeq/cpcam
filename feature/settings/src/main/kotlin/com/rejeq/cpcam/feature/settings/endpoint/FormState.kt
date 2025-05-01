package com.rejeq.cpcam.feature.settings.endpoint

import androidx.compose.ui.text.input.TextFieldValue
import com.rejeq.cpcam.core.data.model.EndpointConfig
import com.rejeq.cpcam.core.data.model.ObsConfig
import com.rejeq.cpcam.core.data.model.ObsStreamData
import com.rejeq.cpcam.core.data.model.PixFmt
import com.rejeq.cpcam.core.data.model.Resolution
import com.rejeq.cpcam.core.data.model.StreamProtocol
import com.rejeq.cpcam.core.data.model.VideoCodec
import com.rejeq.cpcam.core.data.model.VideoConfig

sealed interface FormState<out T> {
    object Loading : FormState<Nothing>
    data class Success<T>(val data: T) : FormState<T>
}

sealed interface EndpointConfigForm {
    fun toDomain(): EndpointConfig
}

fun EndpointConfig.fromDomain(): EndpointConfigForm = when (this) {
    is ObsConfig -> this.fromDomain()
}

data class ObsConfigForm(
    val url: TextFieldValue,
    val port: TextFieldValue,
    val password: TextFieldValue,
) : EndpointConfigForm {
    override fun toDomain(): ObsConfig = ObsConfig(
        url = url.text,
        port = port.text.toIntOrNull() ?: 0,
        password = password.text,
    )
}

fun ObsConfig.fromDomain() = ObsConfigForm(
    url = TextFieldValue(url),
    port = TextFieldValue(port.toString()),
    password = TextFieldValue(password),
)

data class VideoConfigForm(
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

fun VideoConfig.fromDomain() = VideoConfigForm(
    codecName = codecName,
    pixFmt = pixFmt,
    bitrate = TextFieldValue(bitrate?.toString() ?: ""),
    framerate = TextFieldValue(framerate?.toString() ?: ""),
    resolution = Pair(
        TextFieldValue(resolution?.width?.toString() ?: ""),
        TextFieldValue(resolution?.height?.toString() ?: ""),
    ),
)

data class ObsStreamDataForm(
    val protocol: StreamProtocol,
    val host: TextFieldValue,
    val videoConfig: VideoConfigForm,
) {
    fun toDomain(): ObsStreamData = ObsStreamData(
        protocol = protocol,
        host = host.text,
        videoConfig = videoConfig.toDomain(),
    )
}

fun ObsStreamData.fromDomain() = ObsStreamDataForm(
    protocol = protocol,
    host = TextFieldValue(host),
    videoConfig = videoConfig.fromDomain(),
)
