package com.rejeq.cpcam.feature.settings.endpoint.form.field

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rejeq.cpcam.feature.settings.R
import com.rejeq.cpcam.feature.settings.input.Input

@Composable
fun ResolutionFieldContent(
    state: ResolutionFieldState,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    Row(modifier = modifier.focusGroup()) {
        val scope = rememberCoroutineScope()

        Input(
            value = state.width,
            onValueChange = { state.onWidthChange(scope, it) },
            label = stringResource(R.string.resolution_width),
            isError = state.widthError != null,
            supportingText = {
                val error = state.widthError
                if (error != null) {
                    Text(text = error.getStringResource())
                }
            },
            keyboardOptions = keyboardOptions.copy(
                keyboardType = KeyboardType.Number,
            ),
            keyboardActions = keyboardActions,
            modifier = Modifier.weight(1f),
        )

        Spacer(modifier = Modifier.width(8.dp))

        Input(
            value = state.height,
            onValueChange = { state.onHeightChange(scope, it) },
            label = stringResource(R.string.resolution_height),
            isError = state.heightError != null,
            supportingText = {
                val error = state.heightError
                if (error != null) {
                    Text(text = error.getStringResource())
                }
            },
            keyboardOptions = keyboardOptions.copy(
                keyboardType = KeyboardType.Number,
            ),
            keyboardActions = keyboardActions,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
@ReadOnlyComposable
fun ResolutionErrorKind.getStringResource(): String = when (this) {
    ResolutionErrorKind.Negative ->
        stringResource(R.string.resolution_error_negative)
}
