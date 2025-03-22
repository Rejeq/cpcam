package com.rejeq.cpcam.core.ui

import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.app.ActivityCompat

interface PermissionLauncher {
    fun launch()
}

sealed interface PermissionState {
    object Granted : PermissionState
    object Denied : PermissionState
    object PermanentlyDenied : PermissionState
}

@Composable
fun rememberPermissionLauncher(
    permWasLaunched: Boolean,
    permission: String,
    onPermissionResult: (PermissionState) -> Unit,
): PermissionLauncher {
    val activityLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        val state = if (granted) {
            PermissionState.Granted
        } else {
            PermissionState.Denied
        }

        onPermissionResult(state)
    }

    val activity = LocalActivity.current
    return remember(permWasLaunched, permission, activityLauncher) {
        object : PermissionLauncher {
            override fun launch() {
                activity?.let { activity ->
                    val isDenied =
                        !ActivityCompat.shouldShowRequestPermissionRationale(
                            activity,
                            permission,
                        )

                    if (permWasLaunched && isDenied) {
                        onPermissionResult(PermissionState.PermanentlyDenied)
                        return
                    }
                }

                activityLauncher.launch(permission)
            }
        }
    }
}
