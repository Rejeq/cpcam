package com.rejeq.cpcam.feature.main.camera

import android.Manifest
import android.os.Build
import android.util.Log
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.rejeq.cpcam.core.camera.CameraController
import com.rejeq.cpcam.core.camera.CameraStateWrapper
import com.rejeq.cpcam.core.camera.CameraType
import com.rejeq.cpcam.core.camera.target.CameraTarget
import com.rejeq.cpcam.core.common.DndListener
import com.rejeq.cpcam.core.common.DndState
import com.rejeq.cpcam.core.data.repository.AppearanceRepository
import com.rejeq.cpcam.core.ui.PermissionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CameraComponent(
    private val dndListener: DndListener,
    private val scope: CoroutineScope,
    private val appearanceRepo: AppearanceRepository,
    private val controller: CameraController,
    val target: CameraTarget,
    val onShowPermissionDenied: (String) -> Unit,
    componentContext: ComponentContext,
) : ComponentContext by componentContext {
    init {
        lifecycle.doOnDestroy {
            target.stop()
        }
    }

    val state = controller.state.stateIn(
        scope,
        SharingStarted.Eagerly,
        CameraStateWrapper(type = CameraType.Close, error = null),
    )

    val cameraPermission = Manifest.permission.CAMERA
    val isCameraPermissionWasLaunched = appearanceRepo.permissionWasLaunched(
        cameraPermission,
    )

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
}

private const val TAG = "CameraComponent"
