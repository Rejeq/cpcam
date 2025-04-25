package com.rejeq.cpcam.feature.service

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.rejeq.cpcam.core.endpoint.EndpointErrorKind
import com.rejeq.cpcam.core.endpoint.obs.ObsErrorKind
import com.rejeq.cpcam.core.endpoint.obs.ObsStreamErrorKind
import com.rejeq.cpcam.core.stream.StreamErrorKind
import com.rejeq.ktobs.AuthError

@Composable
fun ConnectionErrorContent(
    component: ConnectionErrorComponent,
    modifier: Modifier = Modifier,
) {
    val reason = component.reason.toConnectionError()

    AlertDialog(
        modifier = modifier,
        onDismissRequest = component.onFinished,
        title = {
            Text(text = reason.title)
        },
        text = {
            Text(text = reason.desc)
        },
        confirmButton = {
            TextButton(onClick = {
                component.openEndpointSettings()
                component.onFinished()
            }) {
                Text(
                    text = stringResource(R.string.connection_error_confirm),
                )
            }
        },
    )
}

@Composable
@ReadOnlyComposable
fun EndpointErrorKind.toConnectionError(): ConnectionError = when (this) {
    is EndpointErrorKind.EndpointNotConfigured -> ConnectionError(
        title = stringResource(R.string.endpoint_not_configured_title),
        desc = stringResource(R.string.endpoint_not_configured_desc),
    )
    is EndpointErrorKind.ObsError -> this.kind.toConnectionError()
    is EndpointErrorKind.StreamError -> this.kind.toConnectionError()
    is EndpointErrorKind.UnknownError -> ConnectionError(
        title = stringResource(R.string.endpoint_unknown_error_title),
        desc = stringResource(
            R.string.endpoint_unknown_error_desc,
            this.e.toString(),
        ),
    )
}

@Composable
@ReadOnlyComposable
fun ObsErrorKind.toConnectionError(): ConnectionError = when (this) {
    is ObsErrorKind.AuthFailed -> this.kind.toConnectionError()
    is ObsErrorKind.ConnectionRefused -> ConnectionError(
        title = stringResource(R.string.obs_error_connection_refused_title),
        desc = stringResource(R.string.obs_error_connection_refused_desc),
    )
    is ObsErrorKind.ConnectionTimeout -> ConnectionError(
        title = stringResource(R.string.obs_error_connection_timeout_title),
        desc = stringResource(R.string.obs_error_connection_timeout_desc),
    )
    is ObsErrorKind.NotHaveData -> ConnectionError(
        title = stringResource(R.string.obs_error_no_data_title),
        desc = stringResource(R.string.obs_error_no_data_desc),
    )
    is ObsErrorKind.RequestFailed -> ConnectionError(
        title = stringResource(R.string.obs_error_request_failed_title),
        desc = stringResource(R.string.obs_error_request_failed_desc),
    )
    is ObsErrorKind.Unknown -> ConnectionError(
        title = stringResource(R.string.obs_error_unknown_title),
        desc = stringResource(
            R.string.obs_error_unknown_desc,
            this.e.toString(),
        ),
    )
    is ObsErrorKind.UnknownHost -> ConnectionError(
        title = stringResource(R.string.obs_error_unknown_title),
        desc = stringResource(R.string.obs_error_unknown_desc),
    )
    is ObsErrorKind.UnknownInput -> ConnectionError(
        title = stringResource(R.string.obs_error_unknown_input_title),
        desc = stringResource(R.string.obs_error_unknown_input_desc),
    )
}

@Composable
@ReadOnlyComposable
fun ObsStreamErrorKind.toConnectionError(): ConnectionError = when (this) {
    ObsStreamErrorKind.NoStreamData -> ConnectionError(
        title = stringResource(R.string.stream_error_no_data_title),
        desc = stringResource(R.string.stream_error_no_data_desc),
    )

    is ObsStreamErrorKind.StreamError -> this.kind.toConnectiongError()
}

@Composable
@ReadOnlyComposable
fun StreamErrorKind.toConnectiongError(): ConnectionError = when (this) {
    is StreamErrorKind.NoVideoConfig -> ConnectionError(
        title = stringResource(R.string.stream_error_no_video_config_title),
        desc = stringResource(R.string.stream_error_no_video_config_desc),
    )
    is StreamErrorKind.InvalidVideoStream -> ConnectionError(
        title = stringResource(
            R.string.stream_error_invalid_video_stream_title,
        ),
        desc = stringResource(R.string.stream_error_invalid_video_stream_desc),
    )
    is StreamErrorKind.FFmpegError -> ConnectionError(
        title = stringResource(R.string.stream_error_ffmpeg_title),
        desc = stringResource(
            R.string.stream_error_ffmpeg_desc,
            this.kind.toString(),
        ),
    )
}

@Composable
@ReadOnlyComposable
fun AuthError.toConnectionError() = when (this) {
    is AuthError.InvalidPassword -> ConnectionError(
        title = stringResource(R.string.auth_error_invalid_password_title),
        desc = stringResource(R.string.auth_error_invalid_password_desc),
    )
    is AuthError.InvalidRpc -> ConnectionError(
        title = stringResource(R.string.auth_error_invalid_rpc_title),
        desc = stringResource(R.string.auth_error_invalid_rpc_desc),
    )
    is AuthError.PasswordRequired -> ConnectionError(
        title = stringResource(R.string.auth_error_password_required_title),
        desc = stringResource(R.string.auth_error_password_required_desc),
    )
    is AuthError.Unexpected -> ConnectionError(
        title = stringResource(R.string.auth_error_unexpected_title),
        desc = stringResource(R.string.auth_error_unexpected_desc),
    )
}
