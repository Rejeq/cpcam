package com.rejeq.cpcam.core.camera.repository

import android.annotation.SuppressLint
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.TorchState
import androidx.camera.core.impl.CameraInfoInternal
import androidx.lifecycle.asFlow
import com.rejeq.cpcam.core.camera.CameraController
import com.rejeq.cpcam.core.camera.di.CameraManagerService
import com.rejeq.cpcam.core.camera.query.queryMaxRecordSize
import com.rejeq.cpcam.core.camera.query.querySupportedSizes
import com.rejeq.cpcam.core.camera.source.CameraSource
import com.rejeq.cpcam.core.data.model.Resolution
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class CameraDataRepository @Inject constructor(
    @CameraManagerService private val manager: CameraManager,
    private val source: CameraSource,
) {
    /**
     * The flow of the active camera ID.
     *
     * The flow will emit new id whenever the underlying camera
     * source changes, indicating a switch to a different camera.
     */
    val cameraId = source.camera.map {
        getCurrentCameraId(it)
    }

    /**
     * The ID of the currently active camera.
     */
    val currentCameraId get() = getCurrentCameraId(source.camera.value)

    /**
     * Indicates whether the camera's torch is currently enabled.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val isTorchEnabled = source.camera.flatMapLatest {
        it?.cameraInfo?.torchState?.asFlow() ?: flowOf(TorchState.OFF)
    }.map {
        it == TorchState.ON
    }

    /**
     * Indicates whether the camera has a flash unit.
     *
     * If this property emits `true`, you can control the flash state using the
     * associated [CameraController.enableTorch] method.
     *
     * When no camera is currently available, this flow will emit `false`,
     * indicating no flash unit and no flash control.
     */
    val hasFlashUnit = source.camera.map {
        it?.cameraInfo?.hasFlashUnit() == true
    }

    /**
     * Returns a Flow emitting a list of supported resolutions for a given
     * format.
     *
     * This function retrieves the supported resolutions from the underlying
     * camera source and emits them as a Flow. The Flow will emit a new list
     * whenever the camera source changes.
     *
     * @param format The desired image format (e.g., ImageFormat.JPEG,
     *        ImageFormat.YUV_420_888).
     *        This format determines which stream configuration map will be
     *        queried. Must be a valid ImageFormat constant.
     * @return A Flow emitting a list of [Resolution] objects representing the
     *        supported resolutions for the specified format.
     *        If no resolutions are supported for the given format, the Flow
     *        will emit an empty list.
     *        The flow will emit a new value every time the camera changes.
     */
    fun getSupportedResolutions(format: Int): Flow<List<Resolution>> =
        source.camera.map {
            getCurrentSupportedResolutions(format)
        }

    /**
     * Retrieves a list of supported resolutions for a given image format from
     * the camera source.
     *
     * @param format The desired image format (e.g., ImageFormat.JPEG,
     *        ImageFormat.YUV_420_888).
     *        This format determines which stream configuration map will be
     *        queried. Must be a valid ImageFormat constant.
     * @return A list of [Resolution] objects representing the
     *         supported resolutions.
     *         If the camera is not available or its information cannot be
     *         accessed, an empty list is emitted.
     *
     * @see ImageFormat
     * @see querySupportedSizes
     */
    fun getCurrentSupportedResolutions(format: Int): List<Resolution> {
        val char = getCameraCharacteristics(source)
        if (char == null) {
            Log.e(
                TAG,
                "Unable to query supported resolutions: " +
                    "Unknown characteristics",
            )
            return emptyList()
        }

        return querySupportedSizes(char, format)
    }

    fun getRecordResolutions(format: Int): Flow<List<Resolution>> =
        source.camera.map {
            getCurrentRecordResolutions(format)
        }

    fun getCurrentRecordResolutions(format: Int): List<Resolution> {
        val char = getCameraCharacteristics(source)
        if (char == null) {
            Log.e(
                TAG,
                "Unable to query supported resolutions: " +
                    "Unknown characteristics",
            )
            return emptyList()
        }

        val camId = currentCameraId
        if (camId == null) {
            Log.e(
                TAG,
                "Unable to query supported resolutions: " +
                    "Unknown camera id",
            )
            return emptyList()
        }

        val max = queryMaxRecordSize(char, camId, format)
        if (max == null) {
            Log.e(
                TAG,
                "Unable to query supported resolutions: " +
                    "Unknown maximum record size",
            )
            return emptyList()
        }

        return querySupportedSizes(char, format).filter { it < max }
    }

    @SuppressLint("RestrictedApi")
    private fun getCurrentCameraId(camera: Camera?): String? =
        when (val info = camera?.cameraInfo) {
            null -> source.currId
            is CameraInfoInternal -> info.cameraId
            else -> {
                Log.w(
                    TAG,
                    "Unable to determine camera id: " +
                        "Unknown camera info type '$info' " +
                        "fallback to camera id from camera source",
                )

                source.currId
            }
        }

    @SuppressLint("RestrictedApi")
    private fun getCameraCharacteristics(
        source: CameraSource,
    ): CameraCharacteristics? {
        val info = source.cameraInfo
        return when (info) {
            is CameraInfoInternal -> {
                info.cameraCharacteristics as? CameraCharacteristics
                    ?: manager.getCameraCharacteristics(info.cameraId)
            }
            else -> {
                source.currId?.let { id ->
                    manager.getCameraCharacteristics(id)
                }.apply {
                    if (this == null) {
                        Log.w(
                            TAG,
                            "Unable to query camera characteristics: " +
                                "Camera not started and camera id isn't cached",
                        )
                    }
                }
            }
        }
    }
}

private const val TAG = "CameraDataRepository"
