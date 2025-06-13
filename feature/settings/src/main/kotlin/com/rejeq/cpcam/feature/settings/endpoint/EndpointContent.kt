package com.rejeq.cpcam.feature.settings.endpoint

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.rejeq.cpcam.core.ui.CpcamTopBar
import com.rejeq.cpcam.core.ui.modifier.clearFocusOnTap
import com.rejeq.cpcam.core.ui.theme.CpcamTheme
import com.rejeq.cpcam.feature.settings.R
import com.rejeq.cpcam.feature.settings.endpoint.form.EndpointFormContent
import com.rejeq.cpcam.feature.settings.endpoint.form.EndpointFormState

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
                onBackClick = component::onFinished,
            )
        },
    ) {
        EndpointSettingsContent(
            endpointFormState = component.endpointFormState
                .collectAsState().value,
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
    endpointFormState: EndpointFormState?,
) {
    if (endpointFormState != null) {
        EndpointFormContent(
            state = endpointFormState,
        )
    }
}

@Composable
@Preview
@PreviewScreenSizes
@PreviewLightDark
private fun PreviewEndpointContent() {
    CpcamTheme {
        EndpointContent(
            component = PreviewEndpointComponent(),
        )
    }
}
