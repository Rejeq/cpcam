package com.rejeq.cpcam.core.camera

import android.annotation.SuppressLint
import android.hardware.camera2.CameraManager
import android.util.Log
import androidx.camera.core.CameraControl
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.MeteringPoint
import androidx.camera.core.impl.AdapterCameraInfo
import com.rejeq.cpcam.core.camera.di.CameraManagerService
import com.rejeq.cpcam.core.camera.query.queryNextCameraId
import com.rejeq.cpcam.core.camera.source.CameraSource
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.guava.await

@Singleton
class CameraController @Inject constructor(
    @CameraManagerService private val manager: CameraManager,
    private val source: CameraSource,
) {
    fun switchNextDevice() {
        queryNextCameraId(manager, source.currId)?.let { newId ->
            source.setCameraId(newId)
        }
    }

    suspend fun shiftZoom(
        zoom: Float,
        linear: Boolean = true,
    ): CameraControllerError? {
        val camera = source.camera.value
        val zoomState = camera?.cameraInfo?.zoomState?.value

        return when {
            zoomState == null -> CameraControllerError.CameraNotStarted
            else -> {
                val newZoom = zoomState.linearZoom + zoom

                if (linear) {
                    setLinearZoom(newZoom)
                } else {
                    setZoom(newZoom)
                }
            }
        }
    }

    // FIXME: When new use case bounds to pipeline, the zoom is reset
    suspend fun setLinearZoom(zoom: Float): CameraControllerError? {
        val camera = source.camera.value

        return when {
            zoom !in 0f..1f -> CameraControllerError.ZoomValueOutOfRange
            camera == null -> CameraControllerError.CameraNotStarted
            else -> tryZoomCall {
                val control = camera.cameraControl
                control.setLinearZoom(zoom).await()
            }
        }
    }

    @SuppressLint("RestrictedApi")
    suspend fun setZoom(zoom: Float): CameraControllerError? {
        val camera = source.camera.value
        val zoomState = camera?.cameraInfo?.zoomState?.value

        return when {
            zoomState == null -> CameraControllerError.CameraNotStarted
            else -> {
                val control = camera.cameraControl
                tryZoomCall {
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
     * Enables or disables the torch (flashlight) of the camera.
     *
     * @param state `true` to enable the torch, `false` to disable it.
     * @return A [CameraControllerError] indicating the error of the operation.
     *         null if the operation was successful.
     */
    suspend fun enableTorch(state: Boolean): CameraControllerError? {
        val control = source.camera.value?.cameraControl

        return when {
            control == null -> CameraControllerError.CameraNotStarted
            else -> tryTorchCall {
                control.enableTorch(state).await()
            }
        }
    }

    suspend fun setFocusPoint(point: MeteringPoint): CameraControllerError? {
        val control = source.camera.value?.cameraControl

        return when {
            control == null -> CameraControllerError.CameraNotStarted
            else -> tryFocusCall {
                val action = FocusMeteringAction.Builder(point).build()

                control.startFocusAndMetering(action).await()
            }
        }
    }
}

enum class CameraControllerError {
    Cancelled,
    CameraNotStarted,
    ZoomValueOutOfRange,
    TorchIllegalState,
    FocusNotSupported,
}

private inline fun tryZoomCall(block: () -> Unit): CameraControllerError? =
    try {
        block()
        null
    } catch (e: CameraControl.OperationCanceledException) {
        Log.v(TAG, "Zoom operation was canceled", e)
        CameraControllerError.Cancelled
    } catch (e: IllegalArgumentException) {
        Log.w(TAG, "Zoom value out of range", e)
        CameraControllerError.ZoomValueOutOfRange
    }

private inline fun tryTorchCall(block: () -> Unit): CameraControllerError? =
    try {
        block()
        null
    } catch (e: CameraControl.OperationCanceledException) {
        Log.w(TAG, "Torch operation was canceled", e)
        CameraControllerError.Cancelled
    } catch (e: IllegalStateException) {
        Log.w(TAG, "Torch operation got illegal state", e)
        CameraControllerError.TorchIllegalState
    }

private inline fun tryFocusCall(block: () -> Unit): CameraControllerError? =
    try {
        block()
        null
    } catch (e: CameraControl.OperationCanceledException) {
        Log.w(TAG, "Focus operation was canceled", e)
        CameraControllerError.Cancelled
    } catch (e: IllegalArgumentException) {
        Log.w(TAG, "Focus operation not supported", e)
        CameraControllerError.FocusNotSupported
    }

private const val TAG = "CameraController"
