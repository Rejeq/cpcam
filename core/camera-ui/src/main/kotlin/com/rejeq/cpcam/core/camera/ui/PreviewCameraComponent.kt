package com.rejeq.cpcam.core.camera.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import com.rejeq.cpcam.core.camera.CameraError
import com.rejeq.cpcam.core.camera.SurfaceRequestWrapper
import com.rejeq.cpcam.core.data.model.Resolution
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreviewCameraComponent : CameraComponent {
    override val state = MutableStateFlow(
        CameraPreviewState.Failed(CameraError.PermissionDenied),
    ).asStateFlow()

    override val hasMultipleCameras = MutableStateFlow(true)

    private val _hasTorch = MutableStateFlow(true)
    override val hasTorch = _hasTorch.asStateFlow()

    private val _isTorchEnabled = MutableStateFlow(true)
    override val isTorchEnabled = _isTorchEnabled.asStateFlow()

    private val _focusIndicator =
        MutableStateFlow<FocusIndicatorState>(FocusIndicatorState.Disabled)
    override val focusIndicator = _focusIndicator.asStateFlow()

    override fun onPermissionBlocked(permission: String) {
    }

    override fun onSwitchCamera() {
    }

    override fun onToggleTorch() {
        _isTorchEnabled.value = !isTorchEnabled.value
    }

    override fun onRestartCamera(resolution: Resolution?) {
    }

    override fun onStopCamera() {
    }

    override fun onShiftZoom(zoom: Float) {
    }

    override fun onSetFocus(
        request: SurfaceRequestWrapper,
        offset: Offset,
        transformed: Offset,
    ) {
        _focusIndicator.value = FocusIndicatorState.Focusing(
            IntOffset(offset.x.toInt(), offset.y.toInt()),
        )
    }

    override fun onStartMonitoringDnd() {
    }

    override fun onStopMonitoringDnd() {
    }
}
