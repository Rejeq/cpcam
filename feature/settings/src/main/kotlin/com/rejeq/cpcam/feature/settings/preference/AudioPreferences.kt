package com.rejeq.cpcam.feature.settings.preference

import androidx.compose.runtime.Stable
import com.rejeq.cpcam.feature.settings.item.TextItem

@Stable
data class AudioState(
    val onInputSourceClick: () -> Unit,
    val onRateClick: () -> Unit,
    val onNoiseSuppressionClick: () -> Unit,
)

fun audioPreferences(state: AudioState): List<PreferenceContent> = listOf(
    { modifier ->
        TextItem(
            title = "Input Source",
            subtitle = "Select the microphone source for audio input",
            onClick = state.onInputSourceClick,
            modifier = modifier,
        )
    },
    { modifier ->
        TextItem(
            title = "Rate",
            subtitle = "Select the microphone rate",
            onClick = state.onRateClick,
            modifier = modifier,
        )
    },
    { modifier ->
        TextItem(
            title = "Noise Suppression",
            subtitle = "Chose noise suppression",
            onClick = state.onNoiseSuppressionClick,
            modifier = modifier,
        )
    },
)
