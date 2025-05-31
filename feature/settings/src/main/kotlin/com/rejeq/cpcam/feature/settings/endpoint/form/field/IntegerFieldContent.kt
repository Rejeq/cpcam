package com.rejeq.cpcam.feature.settings.endpoint.form.field

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.rejeq.cpcam.feature.settings.input.Input

@Composable
fun IntegerFieldContent(
    state: IntegerFieldState,
    label: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    Input(
        value = state.value,
        onValueChange = { state.onValueChange(it) },
        label = label,
        modifier = modifier,
        keyboardOptions = keyboardOptions.copy(
            keyboardType = KeyboardType.Number,
        ),
        keyboardActions = keyboardActions,
    )
}
