package com.rejeq.cpcam.feature.main.camera

import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.rejeq.cpcam.core.camera.target.SurfaceRequestState
import com.rejeq.cpcam.core.camera.target.lifecycleObserver
import com.rejeq.cpcam.core.ui.rememberPermissionLauncher

@Composable
fun CameraContent(component: CameraComponent, modifier: Modifier = Modifier) {
    val target = component.target

    val lifecycle = LocalLifecycleOwner.current
    DisposableEffect(lifecycle) {
        val observer = target.lifecycleObserver()
        lifecycle.lifecycle.addObserver(observer)

        onDispose {
            lifecycle.lifecycle.removeObserver(observer)
            target.stop()
        }
    }

    val permLauncher = rememberPermissionLauncher(
        permWasLaunched = component.isCameraPermissionWasLaunched
            .collectAsState(true).value,
        permission = component.cameraPermission,
        onPermissionResult = component::onCameraPermissionResult,
    )

    val state = component.state.collectAsState().value
    val request = target.surfaceRequest.collectAsState().value

    val error = state.error
    if (error != null) {
        CameraErrorContent(
            error,
            onEvent = {
                when (it) {
                    CameraErrorEvent.GrantCameraPermission -> {
                        permLauncher.launch()
                    }
                    CameraErrorEvent.StartMonitoringDnd -> {
                        component.startMonitoringDnd()
                    }
                    CameraErrorEvent.StopMonitoringDnd -> {
                        component.stopMonitoringDnd()
                    }
                }
            },
            modifier = modifier.fillMaxWidth(ERROR_DIALOG_SIZE),
        )
    } else {
        if (request is SurfaceRequestState.Available) {
            CameraPreview(
                request = request.value,
                modifier = modifier.pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, _ ->
                        if (zoom != 1.0f) {
                            component.shiftZoom(-(1.0f - zoom))
                        }
                    }
                },
            )
        }
    }
}

private const val ERROR_DIALOG_SIZE = 0.75f // In percents
