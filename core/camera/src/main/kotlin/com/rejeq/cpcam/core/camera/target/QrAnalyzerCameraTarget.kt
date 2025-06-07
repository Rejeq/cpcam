package com.rejeq.cpcam.core.camera.target

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.UseCase
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
import kotlinx.coroutines.CoroutineScope
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
    val scope: CoroutineScope,
) : CameraTarget<QrRequest> {
    private val targetId = CameraTargetId.Record

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

    private var oldUseCase: UseCase? = null

    override fun start() {
        if (source.isAttached(targetId)) {
            oldUseCase = source.useCases[targetId.ordinal]
        }

        source.attach(targetId, useCase)
    }

    override fun stop() {
        source.detach(targetId)
        _request.value = CameraRequestState.Stopped

        val prevUseCase = oldUseCase
        if (prevUseCase != null) {
            source.attach(targetId, prevUseCase)
            oldUseCase = null
        }
    }
}

class QrRequest(val values: List<String>)
