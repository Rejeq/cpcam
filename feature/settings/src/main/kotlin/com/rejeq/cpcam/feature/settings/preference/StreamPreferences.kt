package com.rejeq.cpcam.feature.settings.preference

import androidx.compose.runtime.Stable
import androidx.compose.ui.res.stringResource
import com.rejeq.cpcam.feature.settings.R
import com.rejeq.cpcam.feature.settings.item.TextItem

@Stable
data class StreamState(
    val onEndpointClick: () -> Unit,
    val onProtocolClick: () -> Unit,
    val onVideoEncoderClick: () -> Unit,
    val onAudioEncoderClick: () -> Unit,
)

fun streamPreferences(state: StreamState): List<PreferenceContent> = listOf(
    { modifier ->
        TextItem(
            title = stringResource(R.string.pref_stream_service_title),
            subtitle = stringResource(R.string.pref_stream_service_desc),
            onClick = state.onEndpointClick,
            modifier = modifier,
        )
    },
    { modifier ->
        TextItem(
            title = stringResource(R.string.pref_stream_protocol_title),
            subtitle = stringResource(R.string.pref_stream_protocol_desc),
            onClick = state.onProtocolClick,
            modifier = modifier,
        )
    },
    { modifier ->
        TextItem(
            title = stringResource(R.string.pref_video_encoder_title),
            subtitle = stringResource(R.string.pref_video_encoder_desc),
            onClick = state.onVideoEncoderClick,
            modifier = modifier,
        )
    },
    { modifier ->
        TextItem(
            title = stringResource(R.string.pref_audio_encoder_title),
            subtitle = stringResource(R.string.pref_audio_encoder_desc),
            onClick = state.onAudioEncoderClick,
            modifier = modifier,
        )
    },
)
