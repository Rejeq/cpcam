package com.rejeq.cpcam.feature.settings.endpoint.form.video

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.rejeq.cpcam.feature.settings.endpoint.form.field.IntegerFieldContent
import com.rejeq.cpcam.feature.settings.endpoint.form.field.ResolutionFieldContent

@Composable
fun VideoFormContent(
    state: FormState<VideoFormState>,
    modifier: Modifier = Modifier,
) {
    var isExpanded = rememberSaveable { mutableStateOf(false) }

    FormContent(
        state = state,
        title = stringResource(R.string.endpoint_video_config_form_title),
        modifier = modifier,
        expandable = true,
        isExpanded = isExpanded.value,
        onHeaderClick = { isExpanded.value = !isExpanded.value },
    ) { state ->
        VideoFormContent(
            state = state,
        )
    }
}

@Composable
fun VideoFormContent(state: VideoFormState) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        EnumFieldContent(
            state = state.codec,
            title = stringResource(R.string.endpoint_video_codec),
            subtitle = "",
        )

        EnumFieldContent(
            state = state.pixFmt,
            title = stringResource(R.string.endpoint_video_format),
            subtitle = "",
        )

        IntegerFieldContent(
            state = state.bitrate,
            label = stringResource(R.string.endpoint_video_bitrate),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
            ),
        )

        IntegerFieldContent(
            state = state.framerate,
            label = stringResource(R.string.endpoint_video_framerate),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
            ),
        )

        ResolutionFieldContent(
            state = state.resolution,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
            ),
        )
    }
}
