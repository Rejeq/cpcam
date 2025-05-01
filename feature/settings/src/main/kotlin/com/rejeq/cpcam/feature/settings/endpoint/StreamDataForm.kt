package com.rejeq.cpcam.feature.settings.endpoint

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.rejeq.cpcam.core.data.model.StreamProtocol
import com.rejeq.cpcam.feature.settings.R
import com.rejeq.cpcam.feature.settings.input.Input

@Composable
fun StreamDataForm(
    state: FormState<ObsStreamDataForm>,
    onChange: (ObsStreamDataForm) -> Unit,
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
fun StreamDataForm(
    state: ObsStreamDataForm,
    onChange: (ObsStreamDataForm) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        val focusManager = LocalFocusManager.current

        Input(
            label = stringResource(
                R.string.endpoint_stream_data_host_input_label,
            ),
            value = state.host,
            onValueChange = { onChange(state.copy(host = it)) },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions {
                focusManager.moveFocus(FocusDirection.Next)
            },
        )

        EnumEntry<StreamProtocol>(
            title = stringResource(
                R.string.endpoint_stream_data_protocol_title,
            ),
            subtitle = "",
            selected = state.protocol,
            onChange = { onChange(state.copy(protocol = it)) },
        )

        Spacer(Modifier.requiredHeight(48.dp))

        VideoConfigForm(
            state = state.videoConfig,
            onChange = { onChange(state.copy(videoConfig = it)) },
        )
    }
}
