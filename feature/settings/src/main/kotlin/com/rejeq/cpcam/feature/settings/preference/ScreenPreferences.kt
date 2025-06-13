package com.rejeq.cpcam.feature.settings.preference

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import com.rejeq.cpcam.core.device.screenOffTimeoutMs
import com.rejeq.cpcam.feature.settings.R
import com.rejeq.cpcam.feature.settings.item.SwitchItem
import com.rejeq.cpcam.feature.settings.item.TextInputItem
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit
import kotlinx.coroutines.flow.StateFlow

@Stable
data class ScreenState(
    val keepScreenAwake: StateFlow<Boolean?>,
    val onKeepScreenAwakeChange: (Boolean) -> Unit,
    val dimScreenDelay: StateFlow<TextFieldValue>,
    val onDimScreenChange: (TextFieldValue) -> Unit,
)

fun screenPreferences(state: ScreenState): List<PreferenceContent> = listOf(
    { modifier ->
        KeepScreenAwakePreference(
            enabled = state.keepScreenAwake.collectAsState().value,
            onChange = state.onKeepScreenAwakeChange,
            modifier = modifier,
        )
    },
    { modifier ->
        DimScreenPreference(
            delay = state.dimScreenDelay.collectAsState().value,
            onChange = state.onDimScreenChange,
            enabled = state.keepScreenAwake.collectAsState().value == true,
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
    val context = LocalContext.current
    val systemDimScreenDelay = remember {
        screenOffTimeoutMs(context.contentResolver)
            ?.milliseconds
            ?.toString(DurationUnit.SECONDS)
    }

    TextInputItem(
        title = stringResource(R.string.pref_dim_screen_title),
        dialogTitle = stringResource(R.string.pref_dim_screen_dialog_title),
        subtitle = stringResource(R.string.pref_dim_screen_desc),
        value = delay,
        onValueChange = onChange,
        placeholder = systemDimScreenDelay,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
        ),
        enabled = enabled,
        modifier = modifier,
    )
}
