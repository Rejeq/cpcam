package com.rejeq.cpcam.feature.settings.endpoint

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rejeq.cpcam.core.data.model.EndpointConfig
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
            endpointFormState = component.endpointForm.collectAsState().value,
            onEndpointChange = component::updateEndpoint,
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
            modifier = Modifier
                .imePadding()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            content = content,
        )
    }
}

@Composable
private fun ColumnScope.EndpointSettingsContent(
    endpointFormState: EndpointFormState,
    onEndpointChange: (EndpointConfig) -> Unit,
    connectionState: EndpointConnectionState,
    onCheckConnection: () -> Unit,
) {
    EndpointForm(
        state = endpointFormState,
        onChange = onEndpointChange,
        onSubmit = onCheckConnection,
    )

    Spacer(Modifier.requiredHeight(48.dp))

    Button(
        onClick = onCheckConnection,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(
                R.string.pref_stream_service_check_connection,
            ),
        )
    }

    val msg = when (connectionState) {
        EndpointConnectionState.NotStarted -> ""
        EndpointConnectionState.Failed -> "Failure"
        EndpointConnectionState.Connecting -> "Connecting"
        EndpointConnectionState.Success -> "Success"
    }

    Text(text = msg)
}
