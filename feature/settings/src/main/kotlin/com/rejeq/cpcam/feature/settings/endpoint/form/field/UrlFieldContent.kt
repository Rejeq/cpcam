package com.rejeq.cpcam.feature.settings.endpoint.form.field

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.rejeq.cpcam.feature.settings.input.Input

@Composable
fun UrlFieldContent(
    label: String,
    state: UrlFieldState,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    Input(
        label = label,
        value = state.url,
        onValueChange = { state.onUrlChange(it) },
        modifier = modifier,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
    )
}
