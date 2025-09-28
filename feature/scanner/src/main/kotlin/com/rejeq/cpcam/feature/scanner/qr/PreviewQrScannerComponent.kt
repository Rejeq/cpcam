package com.rejeq.cpcam.feature.scanner.qr

import android.graphics.Bitmap
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
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

    override val scanState: StateFlow<ScannerButtonState> = MutableStateFlow(
        ScannerButtonState.Analyzing,
    ).asStateFlow()

    override fun onRestartAnalyzer() { }
    override fun onStopAnalyzer() { }
    override fun analyzeBitmap(bitmap: Bitmap) {}
    override fun onFinished() {}
}
