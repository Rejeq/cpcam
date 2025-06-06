package com.rejeq.cpcam.feature.settings.endpoint.form.field

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import com.rejeq.cpcam.feature.settings.R
import com.rejeq.cpcam.feature.settings.input.Input

@Composable
fun IntegerFieldContent(
    state: IntegerFieldState,
    label: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val scope = rememberCoroutineScope()
    val error = state.error?.getStringResource()

    Input(
        value = state.value,
        onValueChange = { state.onValueChange(scope, it) },
        label = label,
        keyboardOptions = keyboardOptions.copy(
            keyboardType = KeyboardType.Number,
        ),
        keyboardActions = keyboardActions,
        isError = state.error != null,
        supportingText = {
            error?.let {
                Text(text = it)
            }
        },
        modifier = modifier.semantics {
            error?.let {
                error(it)
            }
        },
    )
}

@Composable
@ReadOnlyComposable
fun IntegerErrorKind.getStringResource() = when (this) {
    IntegerErrorKind.Negative -> stringResource(R.string.integer_error_negative)
    IntegerErrorKind.NotValid -> stringResource(
        R.string.integer_error_not_valid,
    )
}
