package com.rejeq.cpcam.core.camera.operation

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.CameraControl
import androidx.camera.core.impl.AdapterCameraInfo
import kotlinx.coroutines.guava.await

// FIXME: When new use case bounds to pipeline, the zoom is reset

/**
 * Operation to shift the camera zoom by a specified amount.
 *
 * @property zoom The amount to shift the zoom by
 * @property linear Whether to use linear zoom (true)
 *           or ratio-based zoom (false)
 */
class ShiftZoomOp(
    private val zoom: Float,
    private val linear: Boolean = true,
) : AsyncCameraOperation<ZoomError?> {
    override suspend fun CameraOpExecutor.invoke(): ZoomError? {
        val camera = source.camera.value
        val zoomState = camera?.cameraInfo?.zoomState?.value

        return when {
            zoomState == null -> ZoomError.CameraNotStarted
            else -> {
                val newZoom = zoomState.linearZoom + zoom

                if (linear) {
                    SetLinearZoomOp(newZoom).invoke()
                } else {
                    SetZoomOp(newZoom).invoke()
                }
            }
        }
    }
}

/**
 * Operation to set the camera zoom using linear zoom values (0.0 to 1.0).
 *
 * @property zoom The linear zoom value to set (must be between 0.0 and 1.0)
 */
class SetLinearZoomOp(private val zoom: Float) :
    AsyncCameraOperation<ZoomError?> {
    override suspend fun CameraOpExecutor.invoke(): ZoomError? {
        val camera = source.camera.value

        return when {
            zoom !in 0f..1f -> ZoomError.ZoomValueOutOfRange
            camera == null -> ZoomError.CameraNotStarted
            else -> tryZoomCall {
                val control = camera.cameraControl
                control.setLinearZoom(zoom).await()
            }
        }
    }
}

/**
 * Operation to set the camera zoom using zoom ratio values.
 *
 * @property zoom The zoom ratio value to set
 */
class SetZoomOp(private val zoom: Float) : AsyncCameraOperation<ZoomError?> {
    @SuppressLint("RestrictedApi")
    override suspend fun CameraOpExecutor.invoke(): ZoomError? {
        val camera = source.camera.value
        val zoomState = camera?.cameraInfo?.zoomState?.value

        return when {
            zoomState == null -> ZoomError.CameraNotStarted
            else -> tryZoomCall {
                val control = camera.cameraControl
                val ratio = AdapterCameraInfo.getZoomRatioByPercentage(
                    zoom,
                    zoomState.minZoomRatio,
                    zoomState.maxZoomRatio,
                )

                control.setZoomRatio(ratio).await()
            }
        }
    }
}

/**
 * Possible errors that can occur during zoom operations.
 */
enum class ZoomError {
    /** Operation was cancelled */
    Cancelled,

    /** Camera is not started */
    CameraNotStarted,

    /** Zoom value is out of valid range */
    ZoomValueOutOfRange,
}

private inline fun tryZoomCall(block: () -> Unit): ZoomError? = try {
    block()
    null
} catch (e: CameraControl.OperationCanceledException) {
    Log.v(TAG, "Zoom operation was canceled", e)
    ZoomError.Cancelled
} catch (e: IllegalArgumentException) {
    Log.w(TAG, "Zoom value out of range", e)
    ZoomError.ZoomValueOutOfRange
}

private const val TAG = "CameraZoomOperation"
