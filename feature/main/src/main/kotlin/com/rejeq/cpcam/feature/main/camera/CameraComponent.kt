package com.rejeq.cpcam.feature.main.camera

import android.Manifest
import android.os.Build
import android.util.Log
import androidx.compose.ui.geometry.Offset
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.rejeq.cpcam.core.camera.CameraController
import com.rejeq.cpcam.core.camera.CameraStateWrapper
import com.rejeq.cpcam.core.camera.CameraType
import com.rejeq.cpcam.core.camera.repository.CameraDataRepository
import com.rejeq.cpcam.core.camera.target.PreviewCameraTarget
import com.rejeq.cpcam.core.data.repository.AppearanceRepository
import com.rejeq.cpcam.core.device.DndListener
import com.rejeq.cpcam.core.device.DndState
import com.rejeq.cpcam.core.ui.PermissionState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CameraComponent @AssistedInject constructor(
    private val dndListener: DndListener,
    private val appearanceRepo: AppearanceRepository,
    private val controller: CameraController,
    val target: PreviewCameraTarget,
    cameraDataRepo: CameraDataRepository,
    @Assisted val onShowPermissionDenied: (String) -> Unit,
    @Assisted private val scope: CoroutineScope,
    @Assisted componentContext: ComponentContext,
) : ComponentContext by componentContext {
    init {
        lifecycle.doOnDestroy {
            target.stop()
        }
    }

    val state = cameraDataRepo.state.stateIn(
        scope,
        SharingStarted.Eagerly,
        CameraStateWrapper(type = CameraType.Close, error = null),
    )

    val cameraPermission = Manifest.permission.CAMERA
    val isCameraPermissionWasLaunched = appearanceRepo.permissionWasLaunched(
        cameraPermission,
    )

    val hasTorch = cameraDataRepo.hasFlashUnit
    val isTorchEnabled = cameraDataRepo.isTorchEnabled

    fun onCameraPermissionResult(state: PermissionState) {
        scope.launch {
            appearanceRepo.launchPermission(cameraPermission)

            when (state) {
                PermissionState.Granted -> {
                    restartCamera()
                }
                PermissionState.PermanentlyDenied -> {
                    onShowPermissionDenied(cameraPermission)
                }
                PermissionState.Denied -> { }
            }
        }
    }

    fun switchCamera() {
        scope.launch {
            Log.i(TAG, "Switching camera")

            controller.switchNextDevice()
        }
    }

    fun toggleTorch() {
        scope.launch {
            Log.i(TAG, "Toggling torch")

            val newState = !isTorchEnabled.first()
            controller.enableTorch(newState)
        }
    }

    fun restartCamera() {
        scope.launch {
            Log.i(TAG, "Reopening camera")

            target.start()
        }
    }

    fun shiftZoom(zoom: Float) {
        scope.launch {
            controller.shiftZoom(zoom, linear = true)
        }
    }

    fun setFocus(offset: Offset) {
        scope.launch {
            val point = target.getPoint(offset.x, offset.y)

            if (point != null) {
                controller.setFocusPoint(point)
            }
        }
    }

    fun startMonitoringDnd() {
        // This should never happen, since this error happens only in android 9
        // see camerax CameraState.ERROR_DO_NOT_DISTURB_MODE_ENABLED
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Log.e(TAG, "Unable to start monitoring DND: Unsupported API")
            return
        }

        if (dndListener.currentState == DndState.Disabled) {
            restartCamera()
            return
        }

        dndListener.start { event ->
            if (event == DndState.Disabled) {
                restartCamera()
            }
        }
    }

    fun stopMonitoringDnd() {
        dndListener.stop()
    }

    @AssistedFactory
    interface Factory {
        fun create(
            scope: CoroutineScope,
            componentContext: ComponentContext,
            onShowPermissionDenied: (String) -> Unit,
        ): CameraComponent
    }
}

private const val TAG = "CameraComponent"
