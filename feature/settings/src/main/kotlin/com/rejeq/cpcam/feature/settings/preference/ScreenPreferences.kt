package com.rejeq.cpcam.feature.settings.preference

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import com.rejeq.cpcam.feature.settings.R
import com.rejeq.cpcam.feature.settings.item.SwitchItem
import com.rejeq.cpcam.feature.settings.item.TextInputItem
import kotlinx.coroutines.flow.Flow

@Stable
data class ScreenState(
    val keepScreenAwake: Flow<Boolean>,
    val onKeepScreenAwakeChange: (Boolean) -> Unit,
    val dimScreenDelay: Flow<TextFieldValue>,
    val onDimScreenChange: (TextFieldValue) -> Unit,
)

fun screenPreferences(state: ScreenState): List<PreferenceContent> = listOf(
    { modifier ->
        KeepScreenAwakePreference(
            enabled = state.keepScreenAwake.collectAsState(null).value,
            onChange = state.onKeepScreenAwakeChange,
            modifier = modifier,
        )
    },
    { modifier ->
        DimScreenPreference(
            delay = state.dimScreenDelay.collectAsState(TextFieldValue()).value,
            onChange = state.onDimScreenChange,
            enabled = state.keepScreenAwake.collectAsState(null).value == true,
            modifier = modifier,
        )
    },
)

@Composable
fun KeepScreenAwakePreference(
    enabled: Boolean?,
    onChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    SwitchItem(
        title = stringResource(R.string.pref_keep_screen_awake_title),
        subtitle = stringResource(R.string.pref_keep_screen_awake_desc),
        checked = enabled == true,
        onValueChange = onChange,
        modifier = modifier,
    )
}

@Composable
fun DimScreenPreference(
    delay: TextFieldValue,
    onChange: (TextFieldValue) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    TextInputItem(
        title = stringResource(R.string.pref_dim_screen_title),
        subtitle = stringResource(R.string.pref_dim_screen_desc),
        value = delay,
        onValueChange = onChange,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
        ),
        enabled = enabled,
        modifier = modifier,
    )
}
