package com.rejeq.cpcam.core.ui

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.launch

interface PermissionLauncher {
    fun launch(permission: String)
}

sealed interface PermissionState {
    object Granted : PermissionState
    object Denied : PermissionState
    object PermanentlyDenied : PermissionState
}

@Composable
fun rememberPermissionLauncher(
    onPermissionResult: (PermissionState) -> Unit,
): PermissionLauncher {
    val permStorage = LocalPermissionStorage.current
    val scope = rememberCoroutineScope()

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

    suspend fun handleRequest(perm: String, activity: Activity) {
        val permWasLaunched = permStorage?.wasLaunched(perm) ?: false

        if (permWasLaunched && activity.isPermissionDenied(perm)) {
            onPermissionResult(
                PermissionState.PermanentlyDenied,
            )
            return
        }

        permStorage?.launch(perm)
        activityLauncher.launch(perm)
    }

    val activity = LocalActivity.current
    return remember(activityLauncher) {
        object : PermissionLauncher {
            override fun launch(permission: String) {
                activity?.let { activity ->
                    scope.launch { handleRequest(permission, activity) }
                }
            }
        }
    }
}

interface PermissionStorage {
    suspend fun wasLaunched(permission: String): Boolean
    suspend fun launch(permission: String)
}

val LocalPermissionStorage =
    staticCompositionLocalOf<PermissionStorage?> { null }

private fun Activity.isPermissionDenied(perm: String) =
    !ActivityCompat.shouldShowRequestPermissionRationale(this, perm)
