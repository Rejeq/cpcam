package com.rejeq.cpcam.feature.settings.endpoint

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
import androidx.compose.ui.util.fastForEachIndexed
import com.rejeq.cpcam.core.data.model.PixFmt
import com.rejeq.cpcam.core.data.model.VideoCodec
import com.rejeq.cpcam.feature.settings.R
import com.rejeq.cpcam.feature.settings.input.Input
import com.rejeq.cpcam.feature.settings.input.ResolutionInput
import com.rejeq.cpcam.feature.settings.item.DialogSelectableRow
import com.rejeq.cpcam.feature.settings.item.ListDialogItem
import kotlin.enums.enumEntries

@Composable
fun VideoConfigForm(
    state: FormState<VideoConfigForm>,
    onChange: (VideoConfigForm) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isExpanded = rememberSaveable { mutableStateOf(false) }

    Form(
        state = state,
        title = stringResource(R.string.endpoint_video_config_form_title),
        modifier = modifier,
        expandable = true,
        isExpanded = isExpanded.value,
        onHeaderClick = { isExpanded.value = !isExpanded.value },
    ) { config ->
        VideoConfigForm(
            state = config,
            onChange = onChange,
        )
    }
}

@Composable
fun VideoConfigForm(
    state: VideoConfigForm,
    onChange: (VideoConfigForm) -> Unit,
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

@Composable
inline fun <reified T : Enum<T>> EnumEntry(
    title: String,
    subtitle: String,
    selected: T?,
    crossinline onChange: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    val showDialog = rememberSaveable { mutableStateOf(false) }

    ListDialogItem(
        title = title,
        subtitle = subtitle,
        selected = selected?.toString() ?: "",
        isDialogShown = showDialog.value,
        onDialogDismiss = { showDialog.value = false },
        onItemClick = { showDialog.value = true },
        modifier = modifier,
    ) {
        val entries = enumEntries<T>()

        entries.fastForEachIndexed { idx, entry ->
            item {
                DialogSelectableRow(
                    label = entry.toString(),
                    isSelected = (entries.indexOf(selected) == idx),
                    onSelect = {
                        onChange(entry)
                        showDialog.value = false
                    },
                )
            }
        }
    }
}
