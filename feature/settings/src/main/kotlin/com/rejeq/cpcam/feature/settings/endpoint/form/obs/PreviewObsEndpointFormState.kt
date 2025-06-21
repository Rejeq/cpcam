package com.rejeq.cpcam.feature.settings.endpoint.form.obs

import com.rejeq.cpcam.core.data.model.PixFmt
import com.rejeq.cpcam.core.data.model.Resolution
import com.rejeq.cpcam.core.data.model.StreamProtocol
import com.rejeq.cpcam.core.data.model.VideoCodec
import com.rejeq.cpcam.feature.settings.endpoint.ObsConnectionState
import com.rejeq.cpcam.feature.settings.endpoint.form.FormState
import com.rejeq.cpcam.feature.settings.endpoint.form.stream.StreamFormState
import com.rejeq.cpcam.feature.settings.endpoint.form.video.VideoFormState
import kotlinx.coroutines.flow.MutableStateFlow

class PreviewObsEndpointFormState : ObsEndpointFormState {
    override val configState = MutableStateFlow<FormState<ObsConfigFormState>>(
        FormState.Success(
            ObsConfigFormState(
                initUrl = "ws://localhost:4455",
                initPassword = "preview-password",
                initPort = 4343,
            ),
        ),
    )

    override val streamState = MutableStateFlow<FormState<StreamFormState>>(
        FormState.Success(
            StreamFormState(
                initProtocol = StreamProtocol.MPEGTS,
                initHost = "udp://localhost:12345",
                initAvailableProtocols = emptyList(),
                videoFormState = VideoFormState(
                    initAvailableCodecs = emptyList(),
                    initCodec = VideoCodec.H264,
                    initAvailablePixFmts = emptyList(),
                    getAvailablePixFmts = { emptyList() },
                    initPixFmt = PixFmt.NV12,
                    initBitrate = null,
                    initFramerate = 60,
                    initResolution = Resolution(1920, 1080),
                ),
            ),
        ),
    )

    override val connState = MutableStateFlow(ObsConnectionState.NotStarted)

    override suspend fun saveState() {
    }

    override fun onCheckConnection(formState: ObsConfigFormState) {
        connState.value = ObsConnectionState.Success
    }

    override fun onQrScannerClick() {
    }

    override fun handleQrCode(value: String) {
    }
}
