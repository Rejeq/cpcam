package com.rejeq.cpcam.core.camera.ui

import android.Manifest
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import com.rejeq.cpcam.core.camera.CameraError
import com.rejeq.cpcam.core.camera.SurfaceRequestWrapper
import com.rejeq.cpcam.core.camera.target.CameraRequestState
import com.rejeq.cpcam.core.camera.target.CameraTarget
import com.rejeq.cpcam.core.data.model.Resolution
import com.rejeq.cpcam.core.ui.PermissionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreviewCameraComponent : CameraComponent {
    override val state = MutableStateFlow(
        CameraPreviewState.Failed(CameraError.PermissionDenied),
    ).asStateFlow()

    override val cameraPermission = Manifest.permission.CAMERA

    private val isPermLaunched = MutableStateFlow(false)
    override val isCameraPermissionWasLaunched = isPermLaunched.asStateFlow()

    override val hasMultipleCameras = MutableStateFlow(true)

    private val _hasTorch = MutableStateFlow(true)
    override val hasTorch = _hasTorch.asStateFlow()

    private val _isTorchEnabled = MutableStateFlow(true)
    override val isTorchEnabled = _isTorchEnabled.asStateFlow()

    private val _focusIndicator =
        MutableStateFlow<FocusIndicatorState>(FocusIndicatorState.Disabled)
    override val focusIndicator = _focusIndicator.asStateFlow()

    override val target = object : CameraTarget<SurfaceRequestWrapper> {
        private val _request =
            MutableStateFlow<CameraRequestState<SurfaceRequestWrapper>>(
                CameraRequestState.Stopped,
            )
        override val request = _request.asStateFlow()

        override fun start() {}
        override fun stop() {}
        override fun getPoint(x: Float, y: Float) = null
    }

    override fun provideScreenResolution(resolution: Resolution) {
    }

    override fun onCameraPermissionResult(state: PermissionState) {
    }

    override fun onSwitchCamera() {
    }

    override fun onToggleTorch() {
        _isTorchEnabled.value = !isTorchEnabled.value
    }

    override fun onRestartCamera() {
    }

    override fun onShiftZoom(zoom: Float) {
    }

    override fun onSetFocus(offset: Offset, transformed: Offset) {
        _focusIndicator.value = FocusIndicatorState.Focusing(
            IntOffset(offset.x.toInt(), offset.y.toInt()),
        )
    }

    override fun onStartMonitoringDnd() {
    }

    override fun onStopMonitoringDnd() {
    }
}
