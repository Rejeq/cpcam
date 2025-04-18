package com.rejeq.cpcam.feature.settings.preference

import androidx.compose.runtime.Stable
import androidx.compose.ui.res.stringResource
import com.rejeq.cpcam.feature.settings.R
import com.rejeq.cpcam.feature.settings.item.TextItem

@Stable
data class StreamState(val onEndpointClick: () -> Unit)

fun streamPreferences(state: StreamState): List<PreferenceContent> = listOf(
    { modifier ->
        TextItem(
            title = stringResource(R.string.pref_stream_service_title),
            subtitle = stringResource(R.string.pref_stream_service_desc),
            onClick = state.onEndpointClick,
            modifier = modifier,
        )
    },
)
