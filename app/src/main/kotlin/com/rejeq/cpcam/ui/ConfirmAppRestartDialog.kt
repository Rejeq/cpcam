package com.rejeq.cpcam.ui

import android.content.Context
import android.content.Intent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.rejeq.cpcam.R

class ConfirmAppRestartComponent(val onFinished: () -> Unit) {
    fun onAppRestart(ctx: Context): Nothing = restartApp(ctx)
}

@Composable
fun ConfirmAppRestartDialogContent(
    component: ConfirmAppRestartComponent,
    modifier: Modifier = Modifier,
) {
    val ctx = LocalContext.current

    AlertDialog(
        modifier = modifier,
        onDismissRequest = component.onFinished,
        title = {
            Text(
                text = stringResource(
                    R.string.dialog_confirm_restart_app_title,
                ),
            )
        },
        text = {
            Text(
                text = stringResource(
                    R.string.dialog_confirm_restart_app_desc,
                ),
            )
        },
        confirmButton = {
            TextButton(onClick = {
                component.onAppRestart(ctx)
                component.onFinished()
            }) {
                Text(
                    text = stringResource(
                        R.string.dialog_confirm_restart_app_button,
                    ),
                )
            }
        },
    )
}

internal fun restartApp(context: Context): Nothing {
    val pm = context.packageManager
    val packageName = context.packageName

    val intent = pm.getLaunchIntentForPackage(packageName)
    val mainIntent = Intent.makeRestartActivityTask(
        intent?.component,
    ).apply {
        setPackage(packageName)
    }

    context.startActivity(mainIntent)
    Runtime.getRuntime().exit(0)
    error("Restart app failed")
}
