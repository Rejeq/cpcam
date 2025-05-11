package com.rejeq.cpcam.feature.settings.endpoint

import com.rejeq.cpcam.core.data.model.EndpointType
import com.rejeq.cpcam.feature.settings.endpoint.form.obs.PreviewObsEndpointFormState
import kotlinx.coroutines.flow.MutableStateFlow

class PreviewEndpointComponent : EndpointComponent {
    override val endpointType = MutableStateFlow(EndpointType.OBS)
    override val endpointFormState = MutableStateFlow(
        PreviewObsEndpointFormState(),
    )

    override fun onEndpointTypeChange(type: EndpointType) {
    }

    override fun onFinished() { }
}
