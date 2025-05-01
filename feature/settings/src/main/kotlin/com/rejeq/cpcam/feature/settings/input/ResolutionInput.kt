package com.rejeq.cpcam.feature.settings.input

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.rejeq.cpcam.feature.settings.R

@Composable
fun ResolutionInput(
    value: Pair<TextFieldValue, TextFieldValue>,
    onValueChange: (Pair<TextFieldValue, TextFieldValue>) -> Unit,
    modifier: Modifier = Modifier,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    val (width, height) = value

    Row(modifier = modifier.focusGroup()) {
        Input(
            value = width,
            onValueChange = { onValueChange(Pair(it, height)) },
            label = stringResource(R.string.resolution_width),
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next,
            ),
        )

        Spacer(modifier = Modifier.width(8.dp))

        Input(
            value = height,
            onValueChange = { onValueChange(Pair(width, it)) },
            label = stringResource(R.string.resolution_height),
            modifier = Modifier.weight(1f),
            keyboardActions = keyboardActions,
            keyboardOptions = keyboardOptions.copy(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next,
            ),
        )
    }
}
