package com.rejeq.cpcam.feature.settings.endpoint

import androidx.compose.ui.text.input.TextFieldValue
import com.rejeq.cpcam.core.data.model.PixFmt
import com.rejeq.cpcam.core.data.model.StreamProtocol
import com.rejeq.cpcam.core.data.model.VideoCodec
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreviewEndpointComponent : EndpointComponent {
    private val _endpointConfig =
        MutableStateFlow<FormState<EndpointConfigForm>>(
            FormState.Success(
                ObsConfigForm(
                    url = TextFieldValue("ws://localhost:4455"),
                    password = TextFieldValue("preview-password"),
                    port = TextFieldValue("4343"),
                ),
            ),
        )
    override val endpointConfig = _endpointConfig.asStateFlow()

    private val _streamData = MutableStateFlow<FormState<ObsStreamDataForm>>(
        FormState.Success(
            ObsStreamDataForm(
                protocol = StreamProtocol.MPEGTS,
                host = TextFieldValue("udp://localhost:12345"),
                videoConfig = VideoConfigForm(
                    codecName = VideoCodec.H264,
                    pixFmt = PixFmt.NV12,
                    bitrate = TextFieldValue(""),
                    framerate = TextFieldValue("60"),
                    resolution = TextFieldValue("") to TextFieldValue("1080"),
                ),
            ),
        ),
    )
    override val streamData = _streamData.asStateFlow()

    private val _connectionState =
        MutableStateFlow(EndpointConnectionState.NotStarted)
    override val connectionState = _connectionState.asStateFlow()

    override fun onEndpointChange(data: EndpointConfigForm) {
        _endpointConfig.value = FormState.Success(data)
    }

    override fun onStreamDataChange(data: ObsStreamDataForm) {
        _streamData.value = FormState.Success(data)
    }

    override fun onCheckConnection() {
        _connectionState.value = EndpointConnectionState.Connecting
        _connectionState.value = EndpointConnectionState.Success
    }

    override fun onFinished() { }
}
