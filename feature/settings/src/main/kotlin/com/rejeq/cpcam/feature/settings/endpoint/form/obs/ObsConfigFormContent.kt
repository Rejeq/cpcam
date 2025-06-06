package com.rejeq.cpcam.feature.settings.endpoint.form.obs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rejeq.cpcam.feature.settings.R
import com.rejeq.cpcam.feature.settings.endpoint.ObsConnectionState
import com.rejeq.cpcam.feature.settings.endpoint.form.FormContent
import com.rejeq.cpcam.feature.settings.endpoint.form.FormState
import com.rejeq.cpcam.feature.settings.endpoint.form.field.PasswordFieldContent
import com.rejeq.cpcam.feature.settings.endpoint.form.field.PortFieldContent
import com.rejeq.cpcam.feature.settings.endpoint.form.field.UrlFieldContent
import com.rejeq.cpcam.feature.settings.input.selectAll

@Composable
fun ObsConfigForm(
    state: FormState<ObsConfigFormState>,
    onCheckConnection: (ObsConfigFormState) -> Unit,
    connectionState: ObsConnectionState,
    modifier: Modifier = Modifier,
) {
    FormContent(
        state = state,
        title = stringResource(R.string.endpoint_form_title),
        modifier = modifier,
        expandable = false,
    ) { state ->
        ObsConfigFormContent(
            state = state,
            onCheckConnection = onCheckConnection,
            connectionState = connectionState,
        )
    }
}

@Composable
fun ObsConfigFormContent(
    state: ObsConfigFormState,
    onCheckConnection: (ObsConfigFormState) -> Unit,
    connectionState: ObsConnectionState,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        val focusManager = LocalFocusManager.current
        val keyboardController = LocalSoftwareKeyboardController.current

        UrlFieldContent(
            state = state.url,
            label = stringResource(R.string.endpoint_service_url),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions {
                focusManager.moveFocus(FocusDirection.Next)

                state.port.value = state.port.value.selectAll()
            },
        )

        PortFieldContent(
            state = state.port,
            label = stringResource(R.string.endpoint_service_port),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions {
                focusManager.moveFocus(FocusDirection.Next)

                state.password.password.selectAll()
            },
        )

        PasswordFieldContent(
            state = state.password,
            label = stringResource(R.string.endpoint_service_password),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
            ),
            onKeyboardAction = KeyboardActionHandler {
                keyboardController?.hide()
                onCheckConnection(state)
            },
        )

        Spacer(Modifier.requiredHeight(12.dp))

        ConnectionChecker(
            connectionState = connectionState,
            onCheckConnection = { onCheckConnection(state) },
        )
    }
}

@Composable
private fun ColumnScope.ConnectionChecker(
    connectionState: ObsConnectionState,
    onCheckConnection: () -> Unit,
) {
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
        ObsConnectionState.NotStarted -> ""
        ObsConnectionState.Failed -> "Failure"
        ObsConnectionState.Connecting -> "Connecting"
        ObsConnectionState.Success -> "Success"
    }

    Text(text = msg)
}
