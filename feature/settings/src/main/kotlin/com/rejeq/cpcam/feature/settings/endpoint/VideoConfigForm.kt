package com.rejeq.cpcam.feature.settings.endpoint

import android.util.Log
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.util.fastForEachIndexed
import com.rejeq.cpcam.core.data.model.PixFmt
import com.rejeq.cpcam.core.data.model.Resolution
import com.rejeq.cpcam.core.data.model.VideoCodec
import com.rejeq.cpcam.core.data.model.VideoConfig
import com.rejeq.cpcam.feature.settings.item.DialogRow
import com.rejeq.cpcam.feature.settings.item.ListItem
import kotlin.enums.enumEntries

@Composable
fun ColumnScope.VideoConfigForm(
    state: VideoConfigState,
    onChange: (VideoConfig) -> Unit,
    modifier: Modifier = Modifier,
): Unit = when (state) {
    is VideoConfigState.Loading -> { }
    is VideoConfigState.Success -> {
        VideoConfigForm(
            state.data,
            onChange = onChange,
            modifier = modifier,
        )
    }
}

@Composable
fun ColumnScope.VideoConfigForm(
    state: VideoConfig,
    onChange: (VideoConfig) -> Unit,
    modifier: Modifier = Modifier,
) {
    EnumEntry<PixFmt>(
        title = "Pixel format",
        subtitle = "",
        selected = state.pixFmt ?: PixFmt.RGBA,
        onChange = { onChange(state.copy(pixFmt = it)) },
    )

    EnumEntry<VideoCodec>(
        title = "Pixel format",
        subtitle = "",
        selected = state.codecName ?: VideoCodec.H264,
        onChange = { onChange(state.copy(codecName = it)) },
    )

    Input(
        label = "Bitrate",
        value = state.bitrate?.toString() ?: "",
        onValueChange = {
            val bitrate = it.toIntOrNull()
            if (bitrate != null) {
                onChange(state.copy(bitrate = bitrate))
            } else {
                // TODO: Highlight error
                Log.i("LOGITS", "Unable to convert bitrate to int: '$it'")
            }
        },
    )

    Input(
        label = "Framerate",
        value = state.framerate?.toString() ?: "",
        onValueChange = {
            val framerate = it.toIntOrNull()
            if (framerate != null) {
                onChange(state.copy(framerate = framerate))
            } else {
                // TODO: Highlight error
                Log.i("LOGITS", "Unable to convert framerate to int: '$it'")
            }
        },
    )

    val res = remember { mutableStateOf(state.resolution?.toString() ?: "") }

    Input(
        label = "Resolution",
        value = res.value,
        onValueChange = {
            res.value = it
            val res = Resolution.fromString(it)
            if (res != null) {
                onChange(state.copy(resolution = res))
            } else {
                // TODO: Highlight error
                Log.i("LOGITS", "Unable to convert resolution: '$it'")
            }
        },
    )
}

@Composable
inline fun <reified T : Enum<T>> EnumEntry(
    title: String,
    subtitle: String,
    selected: T,
    crossinline onChange: (T) -> Unit,
) {
    ListItem(
        title = title,
        subtitle = subtitle,
        selected = selected.toString(),
    ) { showDialog ->
        val entries = enumEntries<T>()

        entries.fastForEachIndexed { idx, entry ->
            item {
                DialogRow(
                    label = entry.toString(),
                    isSelected = (entries.indexOf(selected) == idx),
                    showDialog = showDialog,
                    onSelect = { onChange(entry) },
                )
            }
        }
    }
}
