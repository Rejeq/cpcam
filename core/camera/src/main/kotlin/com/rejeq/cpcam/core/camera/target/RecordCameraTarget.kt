package com.rejeq.cpcam.core.camera.target

import android.annotation.SuppressLint
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraMetadata.SCALER_AVAILABLE_STREAM_USE_CASES_VIDEO_CALL
import android.os.Build
import android.util.Log
import android.util.Range
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.Preview
import androidx.camera.core.impl.CameraInfoInternal
import androidx.camera.core.impl.UseCaseConfigFactory.CaptureType
import com.rejeq.cpcam.core.camera.CameraTargetId
import com.rejeq.cpcam.core.camera.SurfaceRequestWrapper
import com.rejeq.cpcam.core.camera.di.MainExecutor
import com.rejeq.cpcam.core.camera.query.isStreamUseCaseSupported
import com.rejeq.cpcam.core.camera.source.CameraSource
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class RecordCameraTarget @Inject constructor(
    private val source: CameraSource,
    @MainExecutor private val executor: Executor,
    val scope: CoroutineScope,
) : CameraTarget {
    private val targetId = CameraTargetId.Record

    private var framerate: Range<Int>? = null

    private val _surfaceRequest = MutableStateFlow<SurfaceRequestState>(
        SurfaceRequestState.Stopped,
    )
    override val surfaceRequest = _surfaceRequest.asStateFlow()

    override fun start() {
        val useCase = buildUseCase(framerate)
        source.attach(targetId, useCase)
    }

    override fun stop() {
        source.detach(targetId)
        _surfaceRequest.value = SurfaceRequestState.Stopped
    }

    /**
     * Sets the desired framerate for the video capture.
     * The framerate will not be applied immediately, but when target is started
     * again
     *
     * @param framerate The desired framerate as a range of integers.
     *        `null` indicates that the framerate should not be explicitly set,
     *        allowing the camerax to choose the best available option.
     */
    fun setFramerate(framerate: Range<Int>?) {
        this.framerate = framerate
    }

    /**
     * Builds a Preview use case for camera this target.
     *
     * We use Preview use case, since it has weaker contract than VideoCapture
     * (VideoCapture optimized to work with mediacodec directly, in our app it
     * not always the case, and sometimes ImageReader fails to retrieve image)
     *
     * @param framerate The desired framerate range for the preview.
     *        If null, the function will not explicitly set a target framerate,
     *        allowing CameraX to determine the optimal framerate.
     * @return A configured Preview use case
     */
    @SuppressLint("RestrictedApi")
    private fun buildUseCase(framerate: Range<Int>?): Preview =
        Preview.Builder().apply {
            setCaptureType(CaptureType.VIDEO_CAPTURE)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val streamUseCase = SCALER_AVAILABLE_STREAM_USE_CASES_VIDEO_CALL
                setStreamUseCase(streamUseCase.toLong())
            }

            if (framerate != null) {
                setTargetFrameRate(framerate)
            }
        }.build().apply {
            executor.execute {
                setSurfaceProvider { newSurfaceRequest ->
                    _surfaceRequest.value =
                        SurfaceRequestState.Available(
                            SurfaceRequestWrapper(newSurfaceRequest),
                        )
                }
            }
        }

    @SuppressLint("RestrictedApi")
    @OptIn(ExperimentalCamera2Interop::class)
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun Preview.Builder.setStreamUseCase(streamUseCase: Long) {
        val cameraInfo = source.camera.value?.cameraInfo
        val cameraCharacteristics =
            (cameraInfo as? CameraInfoInternal)?.cameraCharacteristics
                as? CameraCharacteristics ?: return

        if (!isStreamUseCaseSupported(cameraCharacteristics, streamUseCase)) {
            Log.w(TAG, "Unable to set stream use case: Unsupported")
            return
        }

        Camera2Interop.Extender(this).apply {
            setStreamUseCase(streamUseCase)
        }
    }
}

private const val TAG = "RecordCameraTarget"
