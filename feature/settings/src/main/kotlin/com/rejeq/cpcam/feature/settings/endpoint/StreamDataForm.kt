package com.rejeq.cpcam.feature.settings.endpoint

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.rejeq.cpcam.core.data.model.ObsStreamData
import com.rejeq.cpcam.core.data.model.StreamProtocol

@Composable
fun StreamDataForm(
    state: StreamDataState,
    onChange: (ObsStreamData) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        StreamDataState.Loading -> {}
        is StreamDataState.Success -> StreamDataForm(
            state.data,
            onChange = onChange,
            modifier = modifier,
        )
    }
}

@Composable
fun StreamDataForm(
    state: ObsStreamData,
    onChange: (ObsStreamData) -> Unit,
    modifier: Modifier = Modifier,
) {
    Input(
        label = "Host",
        value = state.host.toString(),
        onValueChange = { onChange(state.copy(host = it)) },
    )

    EnumEntry<StreamProtocol>(
        title = "Stream protocol",
        subtitle = "",
        selected = state.protocol,
        onChange = { onChange(state.copy(protocol = it)) },
    )
}
