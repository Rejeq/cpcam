package com.rejeq.cpcam.feature.settings.endpoint

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.rejeq.cpcam.core.ui.R as CoreR
import com.rejeq.cpcam.feature.settings.R

@Composable
fun Input(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) = TextField(
    value = value,
    keyboardActions = keyboardActions,
    keyboardOptions = keyboardOptions,
    onValueChange = onValueChange,
    label = { Text(text = label) },
    singleLine = true,
    trailingIcon = trailingIcon,
    visualTransformation = visualTransformation,
    // NOTE: Setting fixed height, since TextField changes self height when it
    // become focused and does not contain any value,
    // it also can be fixed with placeholder text
    modifier = modifier.height(56.dp),
)

@Composable
fun PasswordInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Input(
        value = value,
        keyboardActions = keyboardActions,
        keyboardOptions = keyboardOptions,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        visualTransformation = if (!passwordVisible) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    painter = painterResource(
                        if (passwordVisible) {
                            CoreR.drawable.ic_visibility_24dp
                        } else {
                            CoreR.drawable.ic_visibility_off_24dp
                        },
                    ),
                    contentDescription = if (passwordVisible) {
                        stringResource(R.string.endpoint_password_visible_desc)
                    } else {
                        stringResource(R.string.endpoint_password_hidden_desc)
                    },
                )
            }
        },
    )
}

/**
 * A specialized input field for integer values with validation.
 *
 * @param value The current integer value
 * @param onChange Callback when the value changes successfully
 * @param onInvalid Callback when the input is invalid
 * @param label The label text for the input field
 * @param modifier Optional modifier for customizing the layout
 * @param keyboardActions Actions for keyboard events
 * @param keyboardOptions Options for the keyboard input
 */
@Composable
fun IntegerInput(
    value: Int?,
    onChange: (Int?) -> Unit,
    onInvalid: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Number,
    ),
) {
    Input(
        value = value?.toString() ?: "",
        onValueChange = { input ->
            if (input.isEmpty()) {
                onChange(null)
            } else {
                val intValue = input.toIntOrNull()
                if (intValue != null) {
                    onChange(intValue)
                } else {
                    onInvalid(input)
                }
            }
        },
        label = label,
        modifier = modifier,
        keyboardActions = keyboardActions,
        keyboardOptions = keyboardOptions,
    )
}
