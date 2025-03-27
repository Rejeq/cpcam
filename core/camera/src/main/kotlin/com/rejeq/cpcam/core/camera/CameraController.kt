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
import kotlinx.coroutines.CancellationException
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
    ): CameraControllerError {
        val camera = source.camera.value
        if (camera == null) {
            return CameraControllerError.CameraNotStarted
        }

        val zoomState = camera.cameraInfo.zoomState.value
        if (zoomState == null) {
            return CameraControllerError.CameraNotStarted
        }

        val newZoom = zoomState.linearZoom + zoom
        return if (linear) {
            setLinearZoom(newZoom)
        } else {
            setZoom(newZoom)
        }
    }

    // FIXME: When new use case bounds to pipeline, the zoom is reset
    suspend fun setLinearZoom(zoom: Float): CameraControllerError {
        if (zoom !in 0f..1f) {
            return CameraControllerError.ZoomValueOutOfRange
        }

        val camera = source.camera.value
        if (camera == null) {
            return CameraControllerError.CameraNotStarted
        }

        val control = camera.cameraControl

        return tryZoomCall {
            control.setLinearZoom(zoom).await()
        }
    }

    @SuppressLint("RestrictedApi")
    suspend fun setZoom(zoom: Float): CameraControllerError {
        val camera = source.camera.value
        if (camera == null) {
            return CameraControllerError.CameraNotStarted
        }

        val zoomState = camera.cameraInfo.zoomState.value
        if (zoomState == null) {
            return CameraControllerError.CameraNotStarted
        }

        val control = camera.cameraControl
        return tryZoomCall {
            val ratio = AdapterCameraInfo.getZoomRatioByPercentage(
                zoom,
                zoomState.minZoomRatio,
                zoomState.maxZoomRatio,
            )

            control.setZoomRatio(ratio).await()
        }
    }

    /**
     * Enables or disables the torch (flashlight) of the camera.
     *
     * @param state `true` to enable the torch, `false` to disable it.
     * @return A [CameraControllerError] indicating the result of the operation.
     */
    suspend fun enableTorch(state: Boolean): CameraControllerError {
        val control = source.camera.value?.cameraControl
        if (control == null) {
            return CameraControllerError.CameraNotStarted
        }

        try {
            control.enableTorch(state).await()
        } catch (e: CameraControl.OperationCanceledException) {
            Log.w(TAG, "Torch operation was canceled", e)
            return CameraControllerError.TorchCancelled
        } catch (e: IllegalStateException) {
            Log.w(TAG, "Torch operation got illegal state", e)
            return CameraControllerError.TorchIllegalState
        }

        return CameraControllerError.Success
    }

    fun setFocusPoint(state: FocusPointState): CameraControllerError {
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
        return CameraControllerError.Success
    }
}

enum class CameraControllerError {
    Success,
    CameraNotStarted,
    ZoomCancelled,
    ZoomValueOutOfRange,
    TorchCancelled,
    TorchIllegalState,
}

sealed interface FocusPointState {
    object AutoFocus : FocusPointState

    class Point(x: Int, y: Int) : FocusPointState
}

private inline fun tryZoomCall(block: () -> Unit): CameraControllerError {
    try {
        block()
    } catch (e: CameraControl.OperationCanceledException) {
        Log.v(TAG, "Zoom operation was canceled", e)
        return CameraControllerError.ZoomCancelled
    } catch (e: IllegalArgumentException) {
        Log.w(TAG, "Zoom value out of range", e)
        return CameraControllerError.ZoomValueOutOfRange
    } catch (e: Exception) {
        if (e is CancellationException) {
            throw e
        }

        Log.w(TAG, "Failed to set zoom", e)
        return CameraControllerError.ZoomCancelled
    }

    return CameraControllerError.Success
}

private const val TAG = "CameraController"
