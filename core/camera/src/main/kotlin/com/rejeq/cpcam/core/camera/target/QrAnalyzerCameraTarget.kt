package com.rejeq.cpcam.core.camera.target

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.mlkit.vision.MlKitAnalyzer
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.rejeq.cpcam.core.camera.CameraTargetId
import com.rejeq.cpcam.core.camera.di.MainExecutor
import com.rejeq.cpcam.core.camera.source.CameraSource
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.asStateFlow

/**
 * A camera target that analyzes QR codes in the camera feed using ML Kit.
 * Provides a flow of detected QR code values.
 */
@Singleton
class QrAnalyzerCameraTarget @Inject constructor(
    private val source: CameraSource,
    @MainExecutor private val executor: Executor,
) {
    private val targetId = CameraTargetId.Analyzer

    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .build()
    private val barcodeScanner = BarcodeScanning.getClient(options)

    fun start(): CameraRequestFlow<QrRequest> {
        val request = MutableCameraRequestFlow<QrRequest>(
            CameraRequestState.Stopped,
        )
        val useCase = buildUseCase(request)

        source.attach(targetId, useCase)
        return request.asStateFlow()
    }

    fun stop() {
        source.detach(targetId)
    }

    fun analyzeBitmap(bitmap: Bitmap, onResult: (QrRequest) -> Unit) {
        val image = InputImage.fromBitmap(bitmap, 0)

        barcodeScanner.process(image)
            .addOnSuccessListener { barcodes ->
                onResult(barcodes.toQrRequest())
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to analyze bitmap", e)
                onResult(QrRequest(emptyList()))
            }
    }

    private fun buildUseCase(
        request: MutableCameraRequestFlow<QrRequest>,
    ): ImageAnalysis {
        val analyzer = MlKitAnalyzer(
            listOf(barcodeScanner),
            ImageAnalysis.COORDINATE_SYSTEM_ORIGINAL,
            executor,
        ) {
            val result = it?.getValue(barcodeScanner)
                ?.toQrRequest()
                ?: QrRequest(emptyList())

            request.value = CameraRequestState.Available(result)
        }

        return ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .apply {
                setAnalyzer(executor, analyzer)
            }
    }

    private fun List<Barcode>.toQrRequest(): QrRequest = QrRequest(
        this.mapNotNull {
            it.rawValue?.let(::QrData)
        },
    )
}

@JvmInline
value class QrRequest(val values: List<QrData>)

class QrData(val data: String)

private const val TAG = "QrAnalyzerCameraTarget"
