package com.rejeq.cpcam.feature.settings.endpoint.form.video

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.rejeq.cpcam.core.data.model.PixFmt
import com.rejeq.cpcam.core.data.model.VideoCodec
import com.rejeq.cpcam.feature.settings.R
import com.rejeq.cpcam.feature.settings.endpoint.EnumEntry
import com.rejeq.cpcam.feature.settings.endpoint.form.FormContent
import com.rejeq.cpcam.feature.settings.endpoint.form.FormState
import com.rejeq.cpcam.feature.settings.input.Input
import com.rejeq.cpcam.feature.settings.input.ResolutionInput

@Composable
fun VideoConfigForm(
    state: FormState<VideoConfigFormState>,
    onChange: (VideoConfigFormState) -> Unit,
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
        VideoConfigFormContent(
            state = state,
            onChange = onChange,
        )
    }
}

@Composable
fun VideoConfigFormContent(
    state: VideoConfigFormState,
    onChange: (VideoConfigFormState) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        EnumEntry<PixFmt>(
            title = "Pixel format",
            subtitle = "",
            selected = state.pixFmt,
            onChange = { onChange(state.copy(pixFmt = it)) },
            modifier = Modifier.fillMaxWidth(),
        )

        EnumEntry<VideoCodec>(
            title = "Video codec",
            subtitle = "",
            selected = state.codecName,
            onChange = { onChange(state.copy(codecName = it)) },
        )

        Input(
            label = "Bitrate",
            value = state.bitrate,
            onValueChange = { onChange(state.copy(bitrate = it)) },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
            ),
        )

        Input(
            label = "Framerate",
            value = state.framerate,
            onValueChange = { onChange(state.copy(framerate = it)) },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
            ),
        )

        ResolutionInput(
            value = state.resolution,
            onValueChange = { onChange(state.copy(resolution = it)) },
        )
    }
}
