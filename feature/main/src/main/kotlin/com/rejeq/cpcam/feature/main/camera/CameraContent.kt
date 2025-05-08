package com.rejeq.cpcam.feature.main.camera

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.rejeq.cpcam.core.camera.SurfaceRequestWrapper
import com.rejeq.cpcam.core.camera.target.SurfaceRequestState
import com.rejeq.cpcam.core.camera.target.lifecycleObserver
import com.rejeq.cpcam.core.ui.rememberPermissionLauncher
import com.rejeq.cpcam.core.ui.theme.CpcamTheme

@Composable
fun CameraContent(component: CameraComponent, modifier: Modifier = Modifier) {
    val target = component.target

    val lifecycle = LocalLifecycleOwner.current
    DisposableEffect(lifecycle) {
        val observer = target.lifecycleObserver()
        lifecycle.lifecycle.addObserver(observer)

        onDispose {
            lifecycle.lifecycle.removeObserver(observer)
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
                        component.onStartMonitoringDnd()
                    }
                    CameraErrorEvent.StopMonitoringDnd -> {
                        component.onStopMonitoringDnd()
                    }
                }
            },
            modifier = modifier.fillMaxWidth(ERROR_DIALOG_SIZE),
        )
    } else {
        if (request is SurfaceRequestState.Available) {
            CameraPreviewContainer(
                request = request.value,
                onShiftZoom = component::onShiftZoom,
                onFocus = component::onSetFocus,
                focus = component.focusIndicator.collectAsState().value,
            )
        }
    }
}

@Composable
fun CameraPreviewContainer(
    request: SurfaceRequestWrapper,
    onShiftZoom: (Float) -> Unit,
    onFocus: (Offset) -> Unit,
    focus: FocusIndicatorState,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        CameraPreview(
            request = request,
            modifier = Modifier.pointerInput(Unit) {
                detectTransformGestures { _, _, zoom, _ ->
                    if (zoom != 1.0f) {
                        onShiftZoom(-(1.0f - zoom))
                    }
                }
            }.pointerInput(Unit) {
                detectTapGestures { pos ->
                    // FIXME: You need to apply additional transform to the
                    //  position, since the camera surface can not be mapped
                    //  one to one with screen
                    onFocus(pos)
                }
            },
        )

        FocusIndicator(
            state = focus,
        )
    }
}

private const val ERROR_DIALOG_SIZE = 0.75f // In percents
