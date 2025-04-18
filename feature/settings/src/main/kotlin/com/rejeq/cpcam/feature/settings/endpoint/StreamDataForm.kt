package com.rejeq.cpcam.feature.settings.endpoint

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rejeq.cpcam.core.data.model.ObsStreamData
import com.rejeq.cpcam.core.data.model.StreamProtocol
import com.rejeq.cpcam.feature.settings.R

@Composable
fun StreamDataForm(
    state: FormState<ObsStreamData>,
    onChange: (ObsStreamData) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isExpanded = rememberSaveable { mutableStateOf(false) }

    Form(
        state = state,
        title = stringResource(R.string.endpoint_stream_data_form_title),
        modifier = modifier,
        expandable = true,
        isExpanded = isExpanded.value,
        onHeaderClick = { isExpanded.value = !isExpanded.value },
    ) { data ->
        StreamDataForm(
            state = data,
            onChange = onChange,
        )
    }
}

@Composable
fun StreamDataForm(state: ObsStreamData, onChange: (ObsStreamData) -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Input(
            label = stringResource(
                R.string.endpoint_stream_data_host_input_label,
            ),
            value = state.host,
            onValueChange = { onChange(state.copy(host = it)) },
        )

        EnumEntry<StreamProtocol>(
            title = stringResource(
                R.string.endpoint_stream_data_protocol_title,
            ),
            subtitle = "",
            selected = state.protocol,
            onChange = { onChange(state.copy(protocol = it)) },
        )
    }
}
