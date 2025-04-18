package com.rejeq.cpcam.feature.settings.endpoint

import android.util.Log
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.rejeq.cpcam.core.data.model.EndpointConfig
import com.rejeq.cpcam.core.data.model.ObsConfig
import com.rejeq.cpcam.feature.settings.R

@Composable
fun ColumnScope.EndpointForm(
    state: EndpointFormState,
    onChange: (EndpointConfig) -> Unit,
    onSubmit: () -> Unit,
): Unit = when (state) {
    is EndpointFormState.Loading -> { }
    is EndpointFormState.Success -> {
        when (state.data) {
            is ObsConfig -> ObsEndpointForm(
                state.data,
                onChange = onChange,
                onSubmit = onSubmit,
            )
        }
    }
}

@Composable
fun ColumnScope.ObsEndpointForm(
    state: ObsConfig,
    onChange: (ObsConfig) -> Unit,
    onSubmit: () -> Unit,
) {
    val (portFocus, passwordFocus) = remember { FocusRequester.createRefs() }
    val keyboardController = LocalSoftwareKeyboardController.current

    Input(
        value = state.url,
        onValueChange = { onChange(state.copy(url = it)) },
        label = stringResource(R.string.pref_stream_service_url),
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next,
        ),
        keyboardActions = KeyboardActions(
            onNext = {
                portFocus.requestFocus()
            },
        ),
    )

    Input(
        value = state.port.toString(),
        onValueChange = {
            val port = it.toIntOrNull()
            if (port != null) {
                onChange(state.copy(port = port))
            } else {
                // TODO: Highlight error
                Log.i("LOGITS", "Unable to convert port to int: '$it'")
            }
        },
        label = stringResource(R.string.pref_stream_service_port),
        modifier = Modifier.fillMaxWidth().focusRequester(portFocus),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Next,
        ),
        keyboardActions = KeyboardActions(
            onNext = {
                passwordFocus.requestFocus()
            },
        ),
    )

    Input(
        value = state.password,
        onValueChange = { onChange(state.copy(password = it)) },
        label = stringResource(R.string.pref_stream_service_password),
        modifier = Modifier.fillMaxWidth().focusRequester(passwordFocus),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                keyboardController?.hide()
                onSubmit()
            },
        ),
    )
}

