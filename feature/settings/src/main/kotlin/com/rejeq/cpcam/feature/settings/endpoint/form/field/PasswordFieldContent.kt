package com.rejeq.cpcam.feature.settings.endpoint.form.field

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SecureTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.rejeq.cpcam.core.ui.R as CoreR
import com.rejeq.cpcam.feature.settings.R

@Composable
fun PasswordFieldContent(
    state: PasswordFieldState,
    label: String,
    modifier: Modifier = Modifier,
    onKeyboardAction: KeyboardActionHandler? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(state.password) {
        snapshotFlow { state.password.text.toString() }
            .collect { state.onPasswordChange(it) }
    }

    SecureTextField(
        state = state.password,
        modifier = modifier,
        label = { Text(label) },
        textObfuscationMode = if (passwordVisible) {
            TextObfuscationMode.Visible
        } else {
            TextObfuscationMode.RevealLastTyped
        },
        trailingIcon = {
            IconButton(
                onClick = {
                    passwordVisible = !passwordVisible
                },
            ) {
                if (passwordVisible) {
                    Icon(
                        painter = painterResource(
                            CoreR.drawable.ic_visibility_24dp,
                        ),
                        contentDescription = stringResource(
                            R.string.endpoint_password_visible_desc,
                        ),
                    )
                } else {
                    Icon(
                        painter = painterResource(
                            CoreR.drawable.ic_visibility_off_24dp,
                        ),
                        contentDescription = stringResource(
                            R.string.endpoint_password_hidden_desc,
                        ),
                    )
                }
            }
        },
        keyboardOptions = keyboardOptions,
        onKeyboardAction = onKeyboardAction,
    )
}
