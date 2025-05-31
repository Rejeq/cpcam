package com.rejeq.cpcam.feature.settings.endpoint.form

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.rejeq.cpcam.feature.settings.endpoint.form.obs.ObsEndpointFormContent
import com.rejeq.cpcam.feature.settings.endpoint.form.obs.ObsEndpointFormState

interface EndpointFormState {
    // Called when the form is about to be closed
    suspend fun saveState()
}

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

const val DEFAULT_FORM_DEBOUNCE_DELAY = 500L
const val FIELD_ERROR_DELAY = 1500L
