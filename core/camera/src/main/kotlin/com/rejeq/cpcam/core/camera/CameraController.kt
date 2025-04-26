package com.rejeq.cpcam.core.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.hardware.camera2.CameraManager
import android.util.Log
import androidx.camera.core.CameraControl
import androidx.camera.core.impl.AdapterCameraInfo
import androidx.lifecycle.asFlow
import com.rejeq.cpcam.core.camera.di.CameraManagerService
import com.rejeq.cpcam.core.camera.query.queryNextCameraId
import com.rejeq.cpcam.core.camera.source.CameraSource
import com.rejeq.cpcam.core.common.hasPermission
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.guava.await

@Singleton
class CameraController @Inject constructor(
    @ApplicationContext private val context: Context,
    @CameraManagerService private val manager: CameraManager,
    private val source: CameraSource,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    val state = source.cameraInfo.flatMapLatest {
        it.cameraState.asFlow()
    }.map {
        if (!context.hasPermission(Manifest.permission.CAMERA)) {
            return@map CameraStateWrapper(
                CameraType.Close,
                CameraError.PermissionDenied,
            )
        }

        val state = CameraStateWrapper.from(it)
        if (state.error != null) {
            Log.i(TAG, "Got camera error: ${state.error}")
        }

        state
    }

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

    fun setFocusPoint(state: FocusPointState): CameraControllerError? {
        val control = source.camera.value?.cameraControl
        if (control == null) {
            return CameraControllerError.CameraNotStarted
        }

//        val builder = FocusMeteringAction.Builder(meteringPoint)
//
//        when (state) {
//            FocusPointState.AutoFocus -> control.startFocusAndMetering()
//        }

        TODO()
        return null
    }
}

enum class CameraControllerError {
    Cancelled,
    CameraNotStarted,
    ZoomValueOutOfRange,
    TorchIllegalState,
}

sealed interface FocusPointState {
    object AutoFocus : FocusPointState

    class Point(x: Int, y: Int) : FocusPointState
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

private const val TAG = "CameraController"
