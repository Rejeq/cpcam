package com.rejeq.cpcam.core.camera.target

import android.annotation.SuppressLint
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Log
import android.util.Range
import androidx.camera.core.Preview
import androidx.camera.core.impl.CameraInfoInternal
import androidx.camera.core.impl.UseCaseConfigFactory.CaptureType
import androidx.camera.core.resolutionselector.ResolutionSelector
import com.rejeq.cpcam.core.camera.CameraTargetId
import com.rejeq.cpcam.core.camera.di.CameraManagerService
import com.rejeq.cpcam.core.camera.di.MainExecutor
import com.rejeq.cpcam.core.camera.query.queryDefaultRecordSize
import com.rejeq.cpcam.core.camera.query.querySupportedSizes
import com.rejeq.cpcam.core.camera.requireResolution
import com.rejeq.cpcam.core.camera.source.CameraSource
import com.rejeq.cpcam.core.data.model.Resolution
import com.rejeq.cpcam.core.data.repository.CameraRepository
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Singleton
class RecordCameraTarget @Inject constructor(
    private val source: CameraSource,
    private val repository: CameraRepository,
    @MainExecutor private val executor: Executor,
    @CameraManagerService private val cameraManager: CameraManager,
    val scope: CoroutineScope,
) : CameraTarget {
    private val targetId = CameraTargetId.Record

    private val _surfaceRequest = MutableStateFlow<SurfaceRequestState>(
        SurfaceRequestState.Stopped,
    )
    override val surfaceRequest = _surfaceRequest.asStateFlow()

    @SuppressLint("RestrictedApi")
    private val cameraId = source.camera.map {
        when (val info = it?.cameraInfo) {
            null -> source.currId
            is CameraInfoInternal -> info.cameraId
            else -> {
                Log.e(
                    TAG,
                    "Unable to determine camera id: " +
                        "Unknown camera info type '$info'",
                )

                source.currId
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val resolution = cameraId.filterNotNull().flatMapLatest {
        repository.getResolution(it)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val framerate = cameraId.filterNotNull().flatMapLatest {
        repository.getFramerate(it)
    }

    override fun start() {
        scope.launch {
            val useCase = buildUseCase(
                resolution.firstOrNull(),
                framerate.firstOrNull(),
            )

            source.attach(targetId, useCase)
        }
    }

    override fun stop() {
        source.detach(targetId)
        _surfaceRequest.value = SurfaceRequestState.Stopped
    }

    /**
     * Sets the resolution for the currently selected camera.
     *
     * @param resolution The desired resolution to set.
     *        If `null`, it will set the camera to its default resolution.
     *
     * @see CameraRepository.setResolution
     */
    suspend fun setResolution(resolution: Resolution?) = coroutineScope {
        val camId = cameraId.firstOrNull()
        if (camId == null) {
            Log.e(TAG, "Unable to set resolution: Unknown camera id")
            return@coroutineScope
        }

        repository.setResolution(camId, resolution)
    }

    /**
     * Retrieves a list of supported resolutions for a given image format from
     * the camera source.
     *
     * This function queries the underlying camera hardware to determine the
     * supported output sizes for the specified image format. It handles cases
     * where the camera source is not yet available, or if the camera
     * information is not in the expected internal format.
     *
     * @param format The desired image format (e.g., ImageFormat.JPEG,
     *        ImageFormat.YUV_420_888).
     *        This format determines which stream configuration map will be
     *        queried. Must be a valid ImageFormat constant.
     * @return A Flow emitting a List of `Resolution` objects representing the
     *         supported resolutions.
     *         If the camera is not available or its information cannot be
     *         accessed, an empty list is emitted.
     *         Emits a list whenever the camera source is updated.
     *
     * @throws IllegalArgumentException if the `format` is not a valid
     *         ImageFormat constant.
     *
     * @see ImageFormat
     * @see querySupportedSizes
     */
    @SuppressLint("RestrictedApi")
    fun getSupportedResolutions(format: Int): Flow<List<Resolution>> =
        source.camera.map {
            val info = it?.cameraInfo
            return@map when (info) {
                is CameraInfoInternal -> {
                    val char =
                        info.cameraCharacteristics as? CameraCharacteristics
                            ?: cameraManager.getCameraCharacteristics(
                                info.cameraId,
                            )

                    querySupportedSizes(char, format)
                }
                else -> {
                    val id = source.currId
                    if (id == null) {
                        Log.e(
                            TAG,
                            "Unable to query supported resolutions: " +
                                "Camera not started and camera id isn't cached",
                        )

                        return@map emptyList()
                    }

                    val char = cameraManager.getCameraCharacteristics(id)
                    querySupportedSizes(char, format)
                }
            }
        }

    /**
     * Builds a Preview use case for camera this target.
     *
     * We use Preview use case, since it has weaker contract than VideoCapture
     * (VideoCapture optimized to work with mediacodec directly, in our app it
     * not always the case, and sometimes ImageReader fails to retrieve image)
     *
     * @param resolution The desired resolution for the preview.
     *        If null, the default record resolution will be queried
     *        If we fail to query default resolution - CameraX will determine
     *        optimal resolution
     * @param framerate The desired framerate range for the preview.
     *        If null, the function will not explicitly set a target framerate,
     *        allowing CameraX to determine the optimal framerate.
     * @return A configured Preview use case
     */
    @SuppressLint("RestrictedApi")
    private fun buildUseCase(
        resolution: Resolution?,
        framerate: Range<Int>?,
    ): Preview = Preview.Builder().apply {
        setCaptureType(CaptureType.VIDEO_CAPTURE)

        val camera = source.camera.value

        val info = camera?.cameraInfo

        // FIXME: Do not hardcode ImageFormat
        val resolution = resolution ?: when (info) {
            is CameraInfoInternal -> {
                val char =
                    info.cameraCharacteristics as? CameraCharacteristics
                        ?: cameraManager.getCameraCharacteristics(
                            info.cameraId,
                        )

                queryDefaultRecordSize(
                    char,
                    info.cameraId,
                    ImageFormat.YUV_420_888,
                )
            }
            else -> {
                val id = source.currId
                if (id == null) {
                    Log.e(
                        TAG,
                        "Unable to query supported resolutions: " +
                            "Camera not started and camera id isn't cached",
                    )

                    null
                } else {
                    val char = cameraManager.getCameraCharacteristics(id)
                    queryDefaultRecordSize(char, id, ImageFormat.YUV_420_888)
                }
            }
        }

        if (resolution == null) {
            Log.w(
                TAG,
                "Unable to get default resolution, fallback to default camerax",
            )
        } else {
            // FIXME: It is possible that use case will not include particular
            //  resolution specify it explicitly via setSupportedResolutions()
            setResolutionSelector(
                ResolutionSelector.Builder()
                    .requireResolution(resolution)
                    .build(),
            )
        }

        if (framerate != null) {
            setTargetFrameRate(framerate)
        }
    }.build().apply {
        executor.execute {
            setSurfaceProvider { newSurfaceRequest ->
                _surfaceRequest.value =
                    SurfaceRequestState.Available(newSurfaceRequest)
            }
        }
    }
}

private const val TAG = "RecordCameraTarget"
