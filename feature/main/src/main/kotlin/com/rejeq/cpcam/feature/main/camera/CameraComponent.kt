package com.rejeq.cpcam.feature.main.camera

import android.Manifest
import android.os.Build
import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.rejeq.cpcam.core.camera.CameraType
import com.rejeq.cpcam.core.camera.operation.CameraOpExecutor
import com.rejeq.cpcam.core.camera.operation.CameraStateOp
import com.rejeq.cpcam.core.camera.operation.CameraSwitchOp
import com.rejeq.cpcam.core.camera.operation.EnableTorchOp
import com.rejeq.cpcam.core.camera.operation.FocusError
import com.rejeq.cpcam.core.camera.operation.GetCurrentBestPreviewResolutionOp
import com.rejeq.cpcam.core.camera.operation.HasFlashUnitOp
import com.rejeq.cpcam.core.camera.operation.IsTorchEnabledOp
import com.rejeq.cpcam.core.camera.operation.SetFocusPointForTargetOp
import com.rejeq.cpcam.core.camera.operation.ShiftZoomOp
import com.rejeq.cpcam.core.camera.target.CameraTarget
import com.rejeq.cpcam.core.camera.target.PreviewCameraTarget
import com.rejeq.cpcam.core.data.model.Resolution
import com.rejeq.cpcam.core.data.repository.AppearanceRepository
import com.rejeq.cpcam.core.device.DndListener
import com.rejeq.cpcam.core.device.DndState
import com.rejeq.cpcam.core.ui.PermissionState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

interface CameraComponent {
    val state: StateFlow<CameraPreviewState>
    val cameraPermission: String
    val isCameraPermissionWasLaunched: Flow<Boolean>
    val hasTorch: StateFlow<Boolean>
    val isTorchEnabled: StateFlow<Boolean>
    val focusIndicator: StateFlow<FocusIndicatorState>
    val target: CameraTarget

    fun provideScreenResolution(resolution: Resolution)

    fun onCameraPermissionResult(state: PermissionState)
    fun onSwitchCamera()
    fun onToggleTorch()
    fun onRestartCamera()
    fun onShiftZoom(zoom: Float)
    fun onSetFocus(offset: Offset, transformed: Offset)

    fun onStartMonitoringDnd()
    fun onStopMonitoringDnd()
}

class DefaultCameraComponent @AssistedInject constructor(
    private val dndListener: DndListener,
    private val appearanceRepo: AppearanceRepository,
    override val target: PreviewCameraTarget,
    camOpExecutor: CameraOpExecutor,
    @Assisted val onShowPermissionDenied: (String) -> Unit,
    @Assisted private val scope: CoroutineScope,
    @Assisted componentContext: ComponentContext,
) : CameraComponent,
    ComponentContext by componentContext,
    CameraOpExecutor by camOpExecutor {
    init {
        lifecycle.doOnDestroy {
            target.stop()
        }
    }

    private val screenResolution = MutableStateFlow<Resolution?>(null)
    private var lastPreviewSize: Resolution? = null

    override val state = combine(
        CameraStateOp().invoke(),
        target.surfaceRequest,
        screenResolution,
    ) { state, requestState, screenRes ->
        // We must update the preview resolution only when camera is fully
        // opened, to avoid incorrect preview size.
        // When new camera is about to open and old camera has different best
        // preview resolution
        if (state.type == CameraType.Open && screenRes != null) {
            lastPreviewSize =
                GetCurrentBestPreviewResolutionOp(screenRes).invoke()
        }

        state.fromDomain(requestState, lastPreviewSize)
    }.stateIn(
        scope,
        SharingStarted.Eagerly,
        CameraPreviewState.Closed(),
    )

    override val cameraPermission = Manifest.permission.CAMERA
    override val isCameraPermissionWasLaunched =
        appearanceRepo.permissionWasLaunched(cameraPermission)

    override fun onCameraPermissionResult(state: PermissionState) {
        scope.launch {
            appearanceRepo.launchPermission(cameraPermission)

            when (state) {
                PermissionState.Granted -> {
                    onRestartCamera()
                }
                PermissionState.PermanentlyDenied -> {
                    onShowPermissionDenied(cameraPermission)
                }
                PermissionState.Denied -> { }
            }
        }
    }

    override fun provideScreenResolution(resolution: Resolution) {
        screenResolution.value = resolution
    }

    override fun onSwitchCamera() {
        scope.launch {
            CameraSwitchOp().invoke()
        }
    }

    override val hasTorch = HasFlashUnitOp().invoke().stateIn(
        scope,
        SharingStarted.WhileSubscribed(5_000),
        false,
    )

    override val isTorchEnabled = IsTorchEnabledOp().invoke().stateIn(
        scope,
        SharingStarted.WhileSubscribed(5_000),
        false,
    )

    override fun onToggleTorch() {
        scope.launch {
            Log.i(TAG, "Toggling torch")
            val newState = !isTorchEnabled.first()
            EnableTorchOp(newState).invoke()
        }
    }

    override fun onRestartCamera() {
        scope.launch {
            Log.i(TAG, "Reopening camera")
            target.start()
        }
    }

    override fun onShiftZoom(zoom: Float) {
        scope.launch {
            ShiftZoomOp(zoom, linear = true).invoke()
        }
    }

    private val _focusIndicator = MutableStateFlow<FocusIndicatorState>(
        FocusIndicatorState.Disabled,
    )
    override val focusIndicator = _focusIndicator.asStateFlow()

    private var focusJob: Job? = null
    override fun onSetFocus(offset: Offset, transformed: Offset) {
        focusJob?.cancel()
        focusJob = scope.launch {
            val intOffset = IntOffset(offset.x.toInt(), offset.y.toInt())

            _focusIndicator.value = FocusIndicatorState.Focusing(intOffset)

            launch {
                val err = SetFocusPointForTargetOp(
                    transformed.x,
                    transformed.y,
                    target,
                ).invoke()

                if (_focusIndicator.value is FocusIndicatorState.Focusing) {
                    _focusIndicator.value = when (err) {
                        null -> FocusIndicatorState.Focused(intOffset)
                        FocusError.Cancelled -> FocusIndicatorState.Disabled
                        else -> FocusIndicatorState.Failed(intOffset)
                    }
                }
            }

            // Do not disturb user if focus takes too long
            delay(5000)

            _focusIndicator.value = FocusIndicatorState.Disabled
        }
    }

    override fun onStartMonitoringDnd() {
        // This should never happen, since this error happens only in android 9
        // see camerax CameraState.ERROR_DO_NOT_DISTURB_MODE_ENABLED
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Log.e(TAG, "Unable to start monitoring DND: Unsupported API")
            return
        }

        if (dndListener.currentState == DndState.Disabled) {
            onRestartCamera()
            return
        }

        dndListener.start { event ->
            if (event == DndState.Disabled) {
                onRestartCamera()
            }
        }
    }

    override fun onStopMonitoringDnd() {
        dndListener.stop()
    }

    @AssistedFactory
    interface Factory {
        fun create(
            scope: CoroutineScope,
            componentContext: ComponentContext,
            onShowPermissionDenied: (String) -> Unit,
        ): DefaultCameraComponent
    }
}

private const val TAG = "CameraComponent"
