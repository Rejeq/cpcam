package com.rejeq.cpcam.feature.main.camera

import android.graphics.Point
import android.os.Build
import android.view.WindowManager
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.rejeq.cpcam.core.camera.SurfaceRequestWrapper
import com.rejeq.cpcam.core.camera.target.lifecycleObserver
import com.rejeq.cpcam.core.data.model.Resolution
import com.rejeq.cpcam.core.ui.rememberPermissionLauncher

@Composable
fun CameraContent(component: CameraComponent, modifier: Modifier = Modifier) {
    val activity = LocalActivity.current
    if (activity != null) {
        LaunchedEffect(Unit) {
            val res = getScreenResolution(activity.windowManager)
            component.provideScreenResolution(res)
        }
    }

    val lifecycle = LocalLifecycleOwner.current
    DisposableEffect(lifecycle) {
        val observer = component.target.lifecycleObserver()
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
    when (state) {
        is CameraPreviewState.Failed -> {
            CameraErrorContent(
                state.error,
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
        }

        is CameraPreviewState.Closed,
        is CameraPreviewState.Opened,
        -> {
            val request = state.request
            val size = state.size
            if (request != null && size != null) {
                CameraPreviewContainer(
                    request = request,
                    onShiftZoom = component::onShiftZoom,
                    onFocus = component::onSetFocus,
                    focus = component.focusIndicator.collectAsState().value,
                    size = size,
                )
            }
        }
    }
}

@Composable
fun CameraPreviewContainer(
    request: SurfaceRequestWrapper,
    onShiftZoom: (Float) -> Unit,
    onFocus: (Offset) -> Unit,
    focus: FocusIndicatorState,
    size: Resolution,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.background(Color(0, 0, 0)).fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Box {
            CameraPreview(
                request = request,
                size = size,
                modifier = Modifier
                    .pointerInput(Unit) {
                        detectTransformGestures { _, _, zoom, _ ->
                            if (zoom != 1.0f) {
                                onShiftZoom(-(1.0f - zoom))
                            }
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { pos ->
                            // FIXME: You need to apply additional transform to
                            //  the position, since the camera surface can not
                            //  be mapped one to one with screen
                            onFocus(pos)
                        }
                    },
            )

            FocusIndicator(
                state = focus,
            )
        }
    }
}

private fun getScreenResolution(windowManager: WindowManager) =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val bounds = windowManager.maximumWindowMetrics.bounds

        Resolution(bounds.width(), bounds.height())
    } else {
        val point = Point()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getRealSize(point)

        Resolution(point.x, point.y)
    }

private const val ERROR_DIALOG_SIZE = 0.75f // In percents
