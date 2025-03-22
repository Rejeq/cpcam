package com.rejeq.cpcam.feature.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource

@Composable
fun PermissionDeniedContent(
    component: PermissionDeniedComponent,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    AlertDialog(
        modifier = modifier,
        onDismissRequest = component.onFinished,
        title = {
            Text(text = stringResource(R.string.permission_required_title))
        },
        text = {
            val text = when (component.permission) {
                Manifest.permission.CAMERA ->
                    stringResource(R.string.permission_required_camera_desc)
                else ->
                    stringResource(R.string.permission_required_unknown_desc)
            }

            Text(text = text)
        },
        confirmButton = {
            TextButton(onClick = {
                openAppSettings(context)
                component.onFinished()
            }) {
                Text(
                    text = stringResource(
                        R.string.permission_required_open_settings,
                    ),
                )
            }
        },
        dismissButton = {
            TextButton(onClick = component.onFinished) {
                Text(
                    text = stringResource(R.string.permission_required_cancel),
                )
            }
        },
    )
}

private fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
    }

    context.startActivity(intent)
}
