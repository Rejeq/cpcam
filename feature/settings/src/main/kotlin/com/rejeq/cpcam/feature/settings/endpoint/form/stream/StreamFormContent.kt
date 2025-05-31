package com.rejeq.cpcam.feature.settings.endpoint.form.stream

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.rejeq.cpcam.feature.settings.R
import com.rejeq.cpcam.feature.settings.endpoint.form.FormContent
import com.rejeq.cpcam.feature.settings.endpoint.form.FormState
import com.rejeq.cpcam.feature.settings.endpoint.form.field.EnumFieldContent
import com.rejeq.cpcam.feature.settings.endpoint.form.field.UrlFieldContent
import com.rejeq.cpcam.feature.settings.endpoint.form.video.VideoFormContent

@Composable
fun StreamFormContent(
    state: FormState<StreamFormState>,
    modifier: Modifier = Modifier,
) {
    var isExpanded = rememberSaveable { mutableStateOf(false) }

    FormContent(
        state = state,
        title = stringResource(R.string.endpoint_stream_data_form_title),
        modifier = modifier,
        expandable = true,
        isExpanded = isExpanded.value,
        onHeaderClick = { isExpanded.value = !isExpanded.value },
    ) { state ->
        StreamFormContent(
            state = state,
        )
    }
}

@Composable
fun StreamFormContent(state: StreamFormState, modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        UrlFieldContent(
            label = stringResource(
                R.string.endpoint_stream_data_host_input_label,
            ),
            state = state.host,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
            ),
        )

        EnumFieldContent(
            state = state.protocol,
            title = stringResource(
                R.string.endpoint_stream_data_protocol_title,
            ),
            subtitle = "",
        )

        Spacer(Modifier.requiredHeight(48.dp))

        VideoFormContent(
            state = state.videoFormState,
        )
    }
}
