package com.rejeq.cpcam.feature.settings.endpoint.form.obs

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.rejeq.cpcam.core.ui.theme.CpcamTheme
import com.rejeq.cpcam.feature.settings.endpoint.form.stream.StreamFormContent

@Composable
fun ObsEndpointFormContent(
    state: ObsEndpointFormState,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        ObsConfigForm(
            state = state.configState.collectAsState().value,
            onCheckConnection = state::onCheckConnection,
            connectionState = state.connState.collectAsState().value,
            modifier = Modifier.focusGroup(),
        )

        Spacer(Modifier.requiredHeight(48.dp))

        StreamFormContent(
            state = state.streamState.collectAsState().value,
            modifier = Modifier.focusGroup(),
        )
    }
}

@Composable
@PreviewScreenSizes
@PreviewLightDark
fun PreviewObsEndpointFormContent() {
    CpcamTheme {
        ObsEndpointFormContent(
            state = PreviewObsEndpointFormState(),
        )
    }
}
