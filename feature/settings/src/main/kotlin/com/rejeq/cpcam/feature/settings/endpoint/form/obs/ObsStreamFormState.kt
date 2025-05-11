package com.rejeq.cpcam.feature.settings.endpoint.form.obs

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.input.TextFieldValue
import com.rejeq.cpcam.core.data.model.ObsStreamData
import com.rejeq.cpcam.core.data.model.StreamProtocol
import com.rejeq.cpcam.feature.settings.endpoint.form.video.VideoConfigFormState
import com.rejeq.cpcam.feature.settings.endpoint.form.video.fromDomain

@Immutable
data class ObsStreamFormState(
    val protocol: StreamProtocol,
    val host: TextFieldValue,
    val videoConfig: VideoConfigFormState,
) {
    fun toDomain(): ObsStreamData = ObsStreamData(
        protocol = protocol,
        host = host.text,
        videoConfig = videoConfig.toDomain(),
    )
}

fun ObsStreamData.fromDomain() = ObsStreamFormState(
    protocol = protocol,
    host = TextFieldValue(host),
    videoConfig = videoConfig.fromDomain(),
)
