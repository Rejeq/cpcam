package com.rejeq.cpcam.core.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.hardware.camera2.CameraManager
import android.util.Log
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

    @SuppressLint("RestrictedApi")
    fun setZoom(zoom: Float, linear: Boolean = true): CameraControllerError {
        val camera = source.camera.value
        if (camera == null) {
            return CameraControllerError.CameraNotStarted
        }

        val control = camera.cameraControl

        if (linear) {
            // TODO: Handle failure
            control.setLinearZoom(zoom)
        } else {
            val zoomState = camera.cameraInfo.zoomState.value
            if (zoomState == null) {
                // TODO: Better error handling
                return CameraControllerError.CameraNotStarted
            }

            val ratio = AdapterCameraInfo.getZoomRatioByPercentage(
                zoom,
                zoomState.minZoomRatio,
                zoomState.maxZoomRatio,
            )

            // TODO: Handle failure
            control.setZoomRatio(ratio)
        }

        return CameraControllerError.Success
    }

    fun enableTorch(state: Boolean): CameraControllerError {
        val control = source.camera.value?.cameraControl
        if (control == null) {
            return CameraControllerError.CameraNotStarted
        }

        // TODO: Handle failure
        control.enableTorch(state)

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
}

sealed interface FocusPointState {
    object AutoFocus : FocusPointState

    class Point(x: Int, y: Int) : FocusPointState
}

private const val TAG = "CameraController"
