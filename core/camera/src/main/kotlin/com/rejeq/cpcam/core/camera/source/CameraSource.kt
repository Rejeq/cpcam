package com.rejeq.cpcam.core.camera.source

import android.annotation.SuppressLint
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.UseCase
import androidx.camera.core.impl.CameraInfoInternal
import com.rejeq.cpcam.core.camera.CameraTargetId
import com.rejeq.cpcam.core.camera.di.CameraManagerService
import com.rejeq.cpcam.core.camera.query.queryNextCameraId
import com.rejeq.cpcam.core.camera.requireCameraId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.map

/**
 * A class responsible for managing the camera source and its lifecycle.
 *
 * This class handles camera selection, attaching and detaching use cases,
 * and managing the camera pipeline. It utilizes a [CameraLifecycle] to
 * handle the underlying camera operations and lifecycle events.
 *
 * @property lifecycle The [CameraLifecycle] instance managing the camera's
 *           lifecycle.
 * @property currId The ID of the currently selected camera.
 * @property useCases The set of [UseCase] instances currently attached to the
 *           camera.
 * @property camera The underlying Camera object
 */
@Singleton
class CameraSource @Inject constructor(
    private val lifecycle: CameraLifecycle,
    @CameraManagerService val manager: CameraManager,
) {
    var currId: String? = queryNextCameraId(manager, null)
        private set

    private var selector = if (currId != null) {
        CameraSelector.Builder().requireCameraId(currId!!).build()
    } else {
        CameraSelector.DEFAULT_BACK_CAMERA
    }

    val useCases = arrayOfNulls<UseCase?>(CameraTargetId.entries.size)

    val camera = lifecycle.camera

    val cameraInfo = camera.map {
        it?.cameraInfo ?: lifecycle.getCameraInfo(selector)
    }

    /**
     * Checks if a use case with the given [CameraTargetId] is currently
     * attached to the camera.
     *
     * @param id The [CameraTargetId] of the use case to check.
     * @return `true` if a use case with the given ID is attached,
     *         `false` otherwise.
     */
    fun isAttached(id: CameraTargetId) = synchronized(this) {
        useCases[id.ordinal] != null
    }

    /**
     * Attaches a [UseCase] to the pipeline.
     *
     * If this is the first use case being attached, it starts the pipeline.
     * Otherwise it binds the currently attached use cases to the pipeline.
     *
     * It's safe to reattach previously attached useCase without calling
     * [detach] method
     *
     * @param useCase The [UseCase] to attach to this lifecycle.
     */
    fun attach(id: CameraTargetId, useCase: UseCase) = synchronized(this) {
        useCases[id.ordinal] = useCase

        if (!lifecycle.isStarted()) {
            lifecycle.start()
        }

        lifecycle.bindUseCases(
            selector = selector,
            useCases = useCases,
        )
    }

    /**
     * Detaches a [UseCase] from the pipeline.
     *
     * If this is the last useCase, it stops the pipeline.
     * Otherwise it removes the specified useCase id from the pipeline
     *
     * @param id The id of [UseCase] to detach.
     */
    fun detach(id: CameraTargetId) = synchronized(this) {
        useCases[id.ordinal]?.let {
            lifecycle.unbindUseCase(it)
        }

        useCases[id.ordinal] = null

        if (useCases.all { it == null }) {
            lifecycle.stop()
        }
    }

    /**
     * Sets the camera to be used by its ID.
     *
     * This method allows you to switch between different cameras available on
     * the device by specifying their unique ID.
     *
     * After setting the new camera ID, it rebuilds the camera selector and then
     * rebinds all the registered use cases to the new camera via the lifecycle.
     *
     * @param id The unique identifier of the camera to be used. This ID should
     *        be a valid camera ID available on the device.
     */
    fun setCameraId(id: String) = synchronized(this) {
        Log.i(TAG, "Camera id changed to: $id")
        selector = CameraSelector.Builder().requireCameraId(id).build()
        currId = id

        lifecycle.bindUseCases(
            selector = selector,
            useCases = useCases,
        )
    }

    @SuppressLint("RestrictedApi")
    fun getCameraCharacteristics(): CameraCharacteristics? {
        val info = camera.value?.cameraInfo
        val currId = currId
        return when {
            info is CameraInfoInternal -> {
                info.cameraCharacteristics as? CameraCharacteristics
                    ?: manager.getCameraCharacteristics(info.cameraId)
            }

            currId != null -> {
                manager.getCameraCharacteristics(currId)
            }

            else -> {
                Log.w(
                    TAG,
                    "Unable to query camera characteristics: " +
                        "Camera not started and camera id isn't cached",
                )

                null
            }
        }
    }
}

private const val TAG = "CameraSource"
