package com.rejeq.cpcam.feature.scanner.qr

import android.graphics.Bitmap
import android.util.Log
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.rejeq.cpcam.core.camera.target.CameraRequestState
import com.rejeq.cpcam.core.camera.target.CameraTarget
import com.rejeq.cpcam.core.camera.target.QrAnalyzerCameraTarget
import com.rejeq.cpcam.core.camera.target.QrRequest
import com.rejeq.cpcam.core.camera.ui.CameraComponent
import com.rejeq.cpcam.core.camera.ui.DefaultCameraComponent
import com.rejeq.cpcam.core.common.ChildComponent
import com.rejeq.cpcam.core.common.CodeVerifier
import com.rejeq.cpcam.core.common.fastForEach
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

interface QrScannerComponent : ChildComponent {
    val cam: CameraComponent

    val qrAnalyzer: CameraTarget<QrRequest>
    val scanState: StateFlow<ScannerButtonState>

    val nav: QrScannerNavigation

    fun analyzeBitmap(bitmap: Bitmap)
    fun onFinished()
}

class DefaultQrScannerComponent @AssistedInject constructor(
    override val qrAnalyzer: QrAnalyzerCameraTarget,
    cameraFactory: DefaultCameraComponent.Factory,
    @Assisted componentContext: ComponentContext,
    @Assisted mainContext: CoroutineContext,
    @Assisted("onFinished") private val onFinished: (String?) -> Unit,
    @Assisted("verifier") private val verifier: CodeVerifier,
) : QrScannerComponent,
    ComponentContext by componentContext {
    private val scope = coroutineScope(mainContext + SupervisorJob())

    override val nav = DefaultQrScannerNavigation(
        componentContext = this,
    )

    override val cam = cameraFactory.create(
        scope = scope,
        componentContext = this,
        onPermissionBlocked = nav::showPermissionBlocked,
    )

    private val _scanState = MutableStateFlow(ScannerButtonState.Analyzing)
    override val scanState = _scanState.asStateFlow()

    private var oldRequestData: String? = null
    private var resetStateJob: Job? = null

    init {
        qrAnalyzer.request.onEach {
            if (it is CameraRequestState.Available) {
                processQrRequest(it.value)
            }
        }.launchIn(scope)
    }

    override fun analyzeBitmap(bitmap: Bitmap) {
        qrAnalyzer.analyzeBitmap(
            bitmap,
            onResult = ::processQrRequest,
        )
    }

    override fun onFinished() {
        onFinished.invoke(null)
    }

    private fun processQrRequest(request: QrRequest) {
        request.values.fastForEach { value ->
            Log.i(TAG, "scanning QR code: ${value.data}")

            if (oldRequestData != value.data) {
                oldRequestData = value.data

                val ret = verifier.verifyCode(value.data)
                if (ret) {
                    _scanState.value = ScannerButtonState.Analyzing
                    onFinished(value.data)
                } else {
                    _scanState.value = ScannerButtonState.Failed

                    resetStateJob?.cancel()
                    resetStateJob = scope.launch {
                        delay(DEFAULT_RESET_STATE_DELAY)

                        _scanState.value = ScannerButtonState.Analyzing
                    }
                }
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(
            componentContext: ComponentContext,
            mainContext: CoroutineContext,
            @Assisted("onFinished") onFinished: (String?) -> Unit,
            @Assisted("verifier") verifier: CodeVerifier,
        ): DefaultQrScannerComponent
    }
}

private const val TAG = "QrScannerComponent"
private const val DEFAULT_RESET_STATE_DELAY = 5000L
