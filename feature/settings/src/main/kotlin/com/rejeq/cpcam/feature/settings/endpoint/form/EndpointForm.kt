package com.rejeq.cpcam.feature.settings.endpoint.form

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.rejeq.cpcam.feature.settings.endpoint.form.obs.ObsEndpointFormContent
import com.rejeq.cpcam.feature.settings.endpoint.form.obs.ObsEndpointFormState

interface EndpointFormState

@Composable
fun EndpointFormContent(
    state: EndpointFormState,
    modifier: Modifier = Modifier,
) {
    when (state) {
        is ObsEndpointFormState -> ObsEndpointFormContent(
            state,
            modifier = modifier,
        )
    }
}
