package com.rejeq.cpcam.feature.main

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.rejeq.cpcam.core.endpoint.EndpointErrorKind
import com.rejeq.cpcam.core.endpoint.obs.ObsErrorKind
import com.rejeq.cpcam.core.endpoint.obs.StreamErrorKind

@Composable
fun ConnectionErrorContent(
    component: ConnectionErrorComponent,
    modifier: Modifier = Modifier,
) {
    val reason = component.reason

    AlertDialog(
        modifier = modifier,
        onDismissRequest = component.onFinished,
        title = {
            Text(
                text = stringResource(
                    reason.toTitleStringResourceId(),
                ),
            )
        },
        text = {
            Text(text = reason.getDescriptionText())
        },
        confirmButton = {
            TextButton(onClick = component.onFinished) {
                Text(
                    text = stringResource(R.string.connection_error_confirm),
                )
            }
        },
        dismissButton = {
            TextButton(onClick = component.onFinished) {
                Text(
                    text = stringResource(R.string.connection_error_cancel),
                )
            }
        },
    )
}

@Composable
@ReadOnlyComposable
fun EndpointErrorKind.getDescriptionText(): String {
    val id = this.toDescriptionStringResourceId()

    return when (this) {
        is EndpointErrorKind.UnknownError -> stringResource(
            id,
            this.e.toString(),
        )
        else -> stringResource(id)
    }
}

fun EndpointErrorKind.toTitleStringResourceId() = when (this) {
    is EndpointErrorKind.EndpointNotConfigured -> R.string.endpoint_not_configured_title
    is EndpointErrorKind.ObsError -> this.kind.toTitleStringResourceId()
    is EndpointErrorKind.StreamError -> this.kind.toTitleStringResourceId()
    is EndpointErrorKind.UnknownError -> R.string.endpoint_unknown_error_title
}

fun ObsErrorKind.toTitleStringResourceId() = when (this) {
    is ObsErrorKind.AuthFailed -> R.string.obs_error_auth_failed_title
    is ObsErrorKind.ConnectionRefused -> R.string.obs_error_connection_refused_title
    is ObsErrorKind.ConnectionTimeout -> R.string.obs_error_connection_timeout_title
    is ObsErrorKind.NotHaveData -> R.string.obs_error_no_data_title
    is ObsErrorKind.RequestFailed -> R.string.obs_error_request_failed_title
    is ObsErrorKind.Unknown -> R.string.obs_error_unknown_title
    is ObsErrorKind.UnknownHost -> R.string.obs_error_unknown_host_title
    is ObsErrorKind.UnknownInput -> R.string.obs_error_unknown_input_title
}

fun StreamErrorKind.toTitleStringResourceId() = when (this) {
    StreamErrorKind.NoStreamData -> R.string.stream_error_no_data_title
}

fun EndpointErrorKind.toDescriptionStringResourceId() = when (this) {
    is EndpointErrorKind.EndpointNotConfigured -> R.string.endpoint_not_configured_desc
    is EndpointErrorKind.ObsError -> this.kind.toDescriptionStringResourceId()
    is EndpointErrorKind.StreamError -> this.kind.toDescriptionStringResourceId()
    is EndpointErrorKind.UnknownError -> R.string.endpoint_unknown_error_desc
}

fun ObsErrorKind.toDescriptionStringResourceId() = when (this) {
    is ObsErrorKind.AuthFailed -> R.string.obs_error_auth_failed_desc
    is ObsErrorKind.ConnectionRefused -> R.string.obs_error_connection_refused_desc
    is ObsErrorKind.ConnectionTimeout -> R.string.obs_error_connection_timeout_desc
    is ObsErrorKind.NotHaveData -> R.string.obs_error_no_data_desc
    is ObsErrorKind.RequestFailed -> R.string.obs_error_request_failed_desc
    is ObsErrorKind.Unknown -> R.string.obs_error_unknown_desc
    is ObsErrorKind.UnknownHost -> R.string.obs_error_unknown_host_desc
    is ObsErrorKind.UnknownInput -> R.string.obs_error_unknown_input_desc
}

fun StreamErrorKind.toDescriptionStringResourceId() = when (this) {
    StreamErrorKind.NoStreamData -> R.string.stream_error_no_data_desc
}
