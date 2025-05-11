package com.rejeq.cpcam.feature.settings.endpoint.form.obs

import androidx.compose.ui.text.input.TextFieldValue
import com.rejeq.cpcam.core.data.model.PixFmt
import com.rejeq.cpcam.core.data.model.StreamProtocol
import com.rejeq.cpcam.core.data.model.VideoCodec
import com.rejeq.cpcam.feature.settings.endpoint.ObsConnectionState
import com.rejeq.cpcam.feature.settings.endpoint.form.FormState
import com.rejeq.cpcam.feature.settings.endpoint.form.video.VideoConfigFormState
import kotlinx.coroutines.flow.MutableStateFlow

class PreviewObsEndpointFormState : ObsEndpointFormState {
    override val configState = MutableStateFlow<FormState<ObsConfigFormState>>(
        FormState.Success(
            ObsConfigFormState(
                url = TextFieldValue("ws://localhost:4455"),
                password = TextFieldValue("preview-password"),
                port = TextFieldValue("4343"),
            ),
        ),
    )

    override val streamState = MutableStateFlow<FormState<ObsStreamFormState>>(
        FormState.Success(
            ObsStreamFormState(
                protocol = StreamProtocol.MPEGTS,
                host = TextFieldValue("udp://localhost:12345"),
                videoConfig = VideoConfigFormState(
                    codecName = VideoCodec.H264,
                    pixFmt = PixFmt.NV12,
                    bitrate = TextFieldValue(""),
                    framerate = TextFieldValue("60"),
                    resolution = TextFieldValue("") to TextFieldValue("1080"),
                ),
            ),
        ),
    )

    override val connState = MutableStateFlow(ObsConnectionState.NotStarted)

    override fun onConfigChange(newState: ObsConfigFormState) {
        configState.value = FormState.Success(newState)
    }

    override fun onStreamChange(newState: ObsStreamFormState) {
        streamState.value = FormState.Success(newState)
    }

    override fun onCheckConnection(state: ObsConfigFormState) {
        connState.value = ObsConnectionState.Success
    }
}
