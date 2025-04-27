package com.rejeq.cpcam.feature.settings.endpoint

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rejeq.cpcam.core.data.model.EndpointConfig
import com.rejeq.cpcam.core.data.model.ObsConfig
import com.rejeq.cpcam.feature.settings.R

@Composable
fun EndpointForm(
    state: FormState<EndpointConfig>,
    onChange: (EndpointConfig) -> Unit,
    onCheckConnection: () -> Unit,
    connectionState: EndpointConnectionState,
    modifier: Modifier = Modifier,
) {
    Form(
        state = state,
        title = stringResource(R.string.endpoint_form_title),
        modifier = modifier,
        expandable = false,
    ) { config ->
        when (config) {
            is ObsConfig -> ObsEndpointForm(
                config,
                onChange = onChange,
                onCheckConnection = onCheckConnection,
                connectionState = connectionState,
            )
        }
    }
}

@Composable
fun ObsEndpointForm(
    state: ObsConfig,
    onChange: (ObsConfig) -> Unit,
    onCheckConnection: () -> Unit,
    connectionState: EndpointConnectionState,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val (portFocus, passwordFocus) = remember {
            FocusRequester.createRefs()
        }
        val keyboardController = LocalSoftwareKeyboardController.current

        Input(
            value = state.url,
            onValueChange = { onChange(state.copy(url = it)) },
            label = stringResource(R.string.endpoint_service_url),
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

        IntegerInput(
            label = stringResource(R.string.endpoint_service_port),
            value = state.port,
            onChange = { onChange(state.copy(port = it ?: 0)) },
            onInvalid = {
                Log.i("LOGITS", "Unable to convert port to int: '$it'")
            },
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

        PasswordInput(
            value = state.password,
            onValueChange = { onChange(state.copy(password = it)) },
            label = stringResource(R.string.endpoint_service_password),
            modifier = Modifier.fillMaxWidth().focusRequester(passwordFocus),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    onCheckConnection()
                },
            ),
        )

        Spacer(Modifier.requiredHeight(12.dp))

        Button(
            onClick = onCheckConnection,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(
                    R.string.endpoint_service_check_connection,
                ),
            )
        }

        val msg = when (connectionState) {
            EndpointConnectionState.NotStarted -> ""
            EndpointConnectionState.Failed -> "Failure"
            EndpointConnectionState.Connecting -> "Connecting"
            EndpointConnectionState.Success -> "Success"
        }

        Text(text = msg)
    }
}
