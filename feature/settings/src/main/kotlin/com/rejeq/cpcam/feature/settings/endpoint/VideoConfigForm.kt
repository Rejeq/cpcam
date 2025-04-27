package com.rejeq.cpcam.feature.settings.endpoint

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import com.rejeq.cpcam.core.data.model.PixFmt
import com.rejeq.cpcam.core.data.model.Resolution
import com.rejeq.cpcam.core.data.model.VideoCodec
import com.rejeq.cpcam.core.data.model.VideoConfig
import com.rejeq.cpcam.feature.settings.R
import com.rejeq.cpcam.feature.settings.item.DialogSelectableRow
import com.rejeq.cpcam.feature.settings.item.ListDialogItem
import kotlin.enums.enumEntries

@Composable
fun VideoConfigForm(
    state: FormState<VideoConfig>,
    onChange: (VideoConfig) -> Unit,
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
fun VideoConfigForm(state: VideoConfig, onChange: (VideoConfig) -> Unit) {
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
            modifier = Modifier.fillMaxWidth(),
        )

        IntegerInput(
            label = "Bitrate",
            value = state.bitrate,
            onChange = { onChange(state.copy(bitrate = it)) },
            onInvalid = {
                Log.i("LOGITS", "Unable to convert bitrate to int: '$'")
            },
            modifier = Modifier.fillMaxWidth(),
        )

        IntegerInput(
            label = "Framerate",
            value = state.framerate,
            onChange = { onChange(state.copy(framerate = it)) },
            onInvalid = {
                Log.i("LOGITS", "Unable to convert framerate to int: '$it'")
            },
            modifier = Modifier.fillMaxWidth(),
        )

        val res =
            remember { mutableStateOf(state.resolution?.toString() ?: "") }

        Input(
            label = "Resolution",
            value = res.value,
            onValueChange = {
                res.value = it
                if (it.isEmpty()) {
                    onChange(state.copy(resolution = null))
                } else {
                    val resolution = Resolution.fromString(it)
                    if (resolution != null) {
                        onChange(state.copy(resolution = resolution))
                    } else {
                        // TODO: Highlight error
                        Log.i("LOGITS", "Unable to convert resolution: '$it'")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
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
