package com.rejeq.cpcam.feature.settings.preference

import androidx.compose.runtime.Stable
import androidx.compose.ui.res.stringResource
import com.rejeq.cpcam.feature.settings.R
import com.rejeq.cpcam.feature.settings.item.TextItem

@Stable
data class ScreenState(
    val onKeepScreenAwakeClick: () -> Unit,
    val onDimScreenClick: () -> Unit,
)

fun screenPreferences(state: ScreenState): List<PreferenceContent> = listOf(
    { modifier ->
        TextItem(
            title = stringResource(R.string.pref_keep_screen_awake_title),
            subtitle = stringResource(R.string.pref_keep_screen_awake_desc),
            onClick = state.onKeepScreenAwakeClick,
            modifier = modifier,
        )
    },
    { modifier ->
        TextItem(
            title = stringResource(R.string.pref_dim_screen_title),
            subtitle = stringResource(R.string.pref_dim_screen_desc),
            onClick = state.onDimScreenClick,
            modifier = modifier,
        )
    },
)
