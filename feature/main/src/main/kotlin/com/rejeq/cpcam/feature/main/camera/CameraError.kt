package com.rejeq.cpcam.feature.main.camera

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rejeq.cpcam.core.camera.CameraError
import com.rejeq.cpcam.core.ui.theme.CpcamTheme
import com.rejeq.cpcam.feature.main.R

enum class CameraErrorEvent {
    GrantCameraPermission,
    StartMonitoringDnd,
    StopMonitoringDnd,
}

@Composable
fun CameraErrorContent(
    error: CameraError,
    onEvent: (CameraErrorEvent) -> Unit,
    modifier: Modifier = Modifier,
) = when (error) {
    CameraError.PermissionDenied ->
        PermissionDeniedError(
            onGrantCameraPermission = {
                onEvent(CameraErrorEvent.GrantCameraPermission)
            },
            modifier = modifier,
        )
    CameraError.DoNotDisturbEnabled ->
        DndError(
            startMonitoringDnd = {
                onEvent(CameraErrorEvent.StartMonitoringDnd)
            },
            stopMonitoringDnd = {
                onEvent(CameraErrorEvent.StopMonitoringDnd)
            },
            modifier = modifier,
        )
    CameraError.Fatal -> ServiceError(modifier)
    CameraError.InUse -> InUseError(modifier)
    CameraError.Disabled -> DisabledError(modifier)
    else -> UnknownError(modifier)
}

@Composable
private fun PermissionDeniedError(
    onGrantCameraPermission: () -> Unit,
    modifier: Modifier = Modifier,
) = CameraErrorCommon(
    modifier = modifier,
    title = stringResource(R.string.cam_permission_grant_title),
    description = stringResource(R.string.cam_permission_grant_desc),
    icon = {
        Icon(
            modifier = Modifier.requiredSize(48.dp),
            painter = painterResource(R.drawable.ic_no_photography_24dp),
            contentDescription = null,
        )
    },
    action = {
        FilledTonalButton(onClick = onGrantCameraPermission) {
            Text(text = stringResource(R.string.cam_permission_grant_btn))
        }
    },
)

@Composable
private fun DndError(
    startMonitoringDnd: () -> Unit,
    stopMonitoringDnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DisposableEffect(Unit) {
        startMonitoringDnd()

        onDispose {
            stopMonitoringDnd()
        }
    }

    CameraErrorCommon(
        modifier = modifier,
        title = stringResource(R.string.cam_error_dnd_enabled_title),
        description = stringResource(R.string.cam_error_dnd_enabled_desc),
        icon = {
            Icon(
                modifier = Modifier.requiredSize(48.dp),
                painter = painterResource(
                    R.drawable.ic_do_not_disturb_off_24dp,
                ),
                contentDescription = null,
            )
        },
    )
}

@Composable
private fun ServiceError(modifier: Modifier = Modifier) = CameraErrorCommon(
    modifier = modifier,
    title = stringResource(R.string.cam_error_service_title),
    description = stringResource(R.string.cam_error_service_desc),
    icon = {
        Icon(
            modifier = Modifier.requiredSize(48.dp),
            painter = painterResource(R.drawable.ic_error_24dp),
            contentDescription = null,
        )
    },
)

@Composable
private fun InUseError(modifier: Modifier = Modifier) = CameraErrorCommon(
    modifier = modifier,
    title = stringResource(R.string.cam_error_in_use_title),
    description = stringResource(R.string.cam_error_in_use_desc),
    icon = {
        Icon(
            modifier = Modifier.requiredSize(48.dp),
            painter = painterResource(R.drawable.ic_block_24dp),
            contentDescription = null,
        )
    },
)

@Composable
private fun DisabledError(modifier: Modifier = Modifier) = CameraErrorCommon(
    modifier = modifier,
    title = stringResource(R.string.cam_error_disabled_title),
    description = stringResource(R.string.cam_error_disabled_desc),
    icon = {
        Icon(
            modifier = Modifier.requiredSize(48.dp),
            painter = painterResource(R.drawable.ic_block_24dp),
            contentDescription = null,
        )
    },
)

@Composable
private fun UnknownError(modifier: Modifier = Modifier) = CameraErrorCommon(
    modifier = modifier,
    title = stringResource(R.string.cam_error_unknown_title),
    description = stringResource(R.string.cam_error_unknown_desc),
    icon = {
        Icon(
            modifier = Modifier.requiredSize(48.dp),
            painter = painterResource(R.drawable.ic_error_24dp),
            contentDescription = null,
        )
    },
)

@Composable
private fun CameraErrorCommon(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    action: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (icon != null) {
            icon()
        }

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )

        Text(
            text = description,
            textAlign = TextAlign.Center,
        )

        if (action != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                action()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCameraErrorCommon() {
    val desc =
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do " +
            "eiusmod tempor incididunt ut labore et dolore magna aliqua. " +
            "Ut enim ad minim veniam, quis nostrud exercitation ullamco " +
            "laboris nisi ut aliquip ex ea commodo consequat."

    CpcamTheme {
        CameraErrorCommon(
            title = "SomeTitle",
            description = desc,
        )
    }
}
