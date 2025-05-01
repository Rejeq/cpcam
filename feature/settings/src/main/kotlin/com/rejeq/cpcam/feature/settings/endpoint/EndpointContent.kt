package com.rejeq.cpcam.feature.settings.endpoint

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rejeq.cpcam.core.ui.CpcamTopBar
import com.rejeq.cpcam.core.ui.clearFocusOnTap
import com.rejeq.cpcam.feature.settings.R

@Composable
fun EndpointContent(
    component: EndpointComponent,
    modifier: Modifier = Modifier,
) {
    EndpointSettingsLayout(
        modifier = modifier,
        topBar = {
            CpcamTopBar(
                title = stringResource(R.string.pref_stream_service_title),
                onBackClick = component.onFinished,
            )
        },
    ) {
        EndpointSettingsContent(
            endpointFormState = component.endpointConfig.collectAsState().value,
            onEndpointChange = component::updateEndpoint,
            streamDataState = component.streamData.collectAsState().value,
            onStreamDataChange = component::updateStreamData,
            connectionState = component.connectionState.collectAsState().value,
            onCheckConnection = component::checkEndpointConnection,
        )
    }
}

@Composable
private fun EndpointSettingsLayout(
    topBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Scaffold(
        topBar = topBar,
        modifier = modifier.clearFocusOnTap(),
    ) { padding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .imePadding()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            content = content,
        )
    }
}

@Composable
private fun ColumnScope.EndpointSettingsContent(
    endpointFormState: FormState<EndpointConfigForm>,
    onEndpointChange: (EndpointConfigForm) -> Unit,
    streamDataState: FormState<ObsStreamDataForm>,
    onStreamDataChange: (ObsStreamDataForm) -> Unit,
    connectionState: EndpointConnectionState,
    onCheckConnection: () -> Unit,
) {
    EndpointForm(
        state = endpointFormState,
        onChange = onEndpointChange,
        onCheckConnection = onCheckConnection,
        connectionState = connectionState,
        modifier = Modifier.focusGroup(),
    )

    Spacer(Modifier.requiredHeight(48.dp))

    StreamDataForm(
        state = streamDataState,
        onChange = onStreamDataChange,
        modifier = Modifier.focusGroup(),
    )
}
