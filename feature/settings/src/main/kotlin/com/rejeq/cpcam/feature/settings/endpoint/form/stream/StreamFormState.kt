package com.rejeq.cpcam.feature.settings.endpoint.form.stream

import androidx.compose.runtime.Stable
import androidx.compose.runtime.snapshotFlow
import com.rejeq.cpcam.core.data.model.StreamProtocol
import com.rejeq.cpcam.feature.settings.endpoint.form.field.EnumFieldState
import com.rejeq.cpcam.feature.settings.endpoint.form.field.UrlFieldState
import com.rejeq.cpcam.feature.settings.endpoint.form.video.VideoFormData
import com.rejeq.cpcam.feature.settings.endpoint.form.video.VideoFormState

@Stable
class StreamFormState(
    initHost: String,
    initProtocol: StreamProtocol,
    initAvailableProtocols: List<StreamProtocol>,

    val videoFormState: VideoFormState,
) {
    val protocol = EnumFieldState<StreamProtocol>(
        initSelected = initProtocol,
        initAvailables = initAvailableProtocols,
    )

    val host = UrlFieldState(initHost)

    val state = snapshotFlow {
        StreamFormData(
            protocol = protocol.state,
            host = host.state,
            videoForm = videoFormState.state,
        )
    }
}

data class StreamFormData(
    val protocol: StreamProtocol,
    val host: String,
    val videoForm: VideoFormData,
)
