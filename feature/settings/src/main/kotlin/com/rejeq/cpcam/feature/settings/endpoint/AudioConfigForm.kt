package com.rejeq.cpcam.feature.settings.endpoint

import android.util.Log
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.rejeq.cpcam.core.data.model.AudioCodec
import com.rejeq.cpcam.core.data.model.AudioConfig
import com.rejeq.cpcam.core.data.model.SampleFormat

@Composable
fun ColumnScope.AudioConfigForm(
    state: AudioConfigState,
    onChange: (AudioConfig) -> Unit,
    modifier: Modifier = Modifier,
): Unit = when (state) {
    is AudioConfigState.Loading -> { }
    is AudioConfigState.Success -> {
        AudioConfigForm(
            state.data,
            onChange = onChange,
            modifier = modifier,
        )
    }
}

@Composable
fun ColumnScope.AudioConfigForm(
    state: AudioConfig,
    onChange: (AudioConfig) -> Unit,
    modifier: Modifier = Modifier,
) {
    EnumEntry<AudioCodec>(
        title = "Audio codec",
        subtitle = "",
        selected = state.codecName,
        onChange = { onChange(state.copy(codecName = it)) },
    )

    EnumEntry<SampleFormat>(
        title = "Sample format",
        subtitle = "",
        selected = state.format,
        onChange = { onChange(state.copy(format = it)) },
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
        label = "Sample Rate",
        value = state.sampleRate?.toString() ?: "",
        onValueChange = {
            val sampleRate = it.toIntOrNull()
            if (sampleRate != null) {
                onChange(state.copy(sampleRate = sampleRate))
            } else {
                // TODO: Highlight error
                Log.i("LOGITS", "Unable to convert sampleRate to int: '$it'")
            }
        },
    )

    Input(
        label = "Channel count",
        value = state.channelCount?.toString() ?: "",
        onValueChange = {
            val channelCount = it.toIntOrNull()
            if (channelCount != null) {
                onChange(state.copy(channelCount = channelCount))
            } else {
                // TODO: Highlight error
                Log.i("LOGITS", "Unable to convert channelCount to int: '$it'")
            }
        },
    )
}
