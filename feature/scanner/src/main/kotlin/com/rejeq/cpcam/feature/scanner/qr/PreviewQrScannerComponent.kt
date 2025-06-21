package com.rejeq.cpcam.feature.scanner.qr

import android.graphics.Bitmap
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.rejeq.cpcam.core.camera.target.CameraRequestState
import com.rejeq.cpcam.core.camera.target.CameraTarget
import com.rejeq.cpcam.core.camera.target.QrRequest
import com.rejeq.cpcam.core.camera.ui.PreviewCameraComponent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreviewQrScannerNavigation : QrScannerNavigation {
    override val dialog: Value<ChildSlot<*, QrScannerNavigation.Dialog>> =
        MutableValue(ChildSlot<Any, QrScannerNavigation.Dialog>(null))

    override fun showPermissionBlocked(permission: String) {}
}

class PreviewQrScannerComponent : QrScannerComponent {
    override val nav = PreviewQrScannerNavigation()
    override val cam = PreviewCameraComponent()

    override val qrAnalyzer = object : CameraTarget<QrRequest> {
        override val request = MutableStateFlow(
            CameraRequestState.Stopped,
        ).asStateFlow()

        override fun start() {
        }

        override fun stop() {
        }
    }

    override val scanState: StateFlow<ScannerButtonState> = MutableStateFlow(
        ScannerButtonState.Analyzing,
    ).asStateFlow()

    override fun analyzeBitmap(bitmap: Bitmap) {}
    override fun onFinished() {}
}
