package com.rejeq.cpcam.core.camera.target

import androidx.camera.core.ImageAnalysis
import androidx.camera.mlkit.vision.MlKitAnalyzer
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.rejeq.cpcam.core.camera.CameraTargetId
import com.rejeq.cpcam.core.camera.di.MainExecutor
import com.rejeq.cpcam.core.camera.source.CameraSource
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A camera target that analyzes QR codes in the camera feed using ML Kit.
 * Provides a flow of detected QR code values.
 */
@Singleton
class QrAnalyzerCameraTarget @Inject constructor(
    private val source: CameraSource,
    @MainExecutor private val executor: Executor,
) : CameraTarget<QrRequest> {
    private val targetId = CameraTargetId.Analyzer

    private val _request = MutableStateFlow<CameraRequestState<QrRequest>>(
        CameraRequestState.Stopped,
    )
    override val request = _request.asStateFlow()

    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .build()
    private val barcodeScanner = BarcodeScanning.getClient(options)

    private val useCase = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()
        .apply {
            setAnalyzer(
                executor,
                MlKitAnalyzer(
                    listOf(barcodeScanner),
                    ImageAnalysis.COORDINATE_SYSTEM_ORIGINAL,
                    executor,
                ) { result ->
                    val tmp = QrRequest(
                        result?.getValue(barcodeScanner)
                            ?.mapNotNull { it.rawValue }
                            ?: emptyList(),
                    )

                    _request.value = CameraRequestState.Available(tmp)
                },
            )
        }

    override fun start() {
        source.attach(targetId, useCase)
    }

    override fun stop() {
        source.detach(targetId)
        _request.value = CameraRequestState.Stopped

    }
}

class QrRequest(val values: List<String>)
