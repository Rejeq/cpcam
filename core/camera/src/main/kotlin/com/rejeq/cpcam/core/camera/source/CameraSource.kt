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
 * It enforces a priority-based system for managing [UseCase] attachments,
 * ensuring that only a supported number of use cases are active at any
 * given time. The priority is as follows: Preview > Analyzer > Record.
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

    private val useCases = arrayOfNulls<UseCase?>(CameraTargetId.entries.size)

    private val pipelineUseCases =
        arrayOfNulls<UseCase?>(MAX_CONCURRENT_USE_CASES)

    val camera = lifecycle.camera

    val cameraInfo = camera.map {
        it?.cameraInfo ?: lifecycle.getCameraInfo(selector)
    }

    /**
     * Checks if a use case for the given [CameraTargetId] has been requested.
     *
     * Note that a requested use case is not guaranteed to be active in the
     * pipeline, as it may be superseded by a higher-priority use case.
     *
     * @param id The [CameraTargetId] of the use case to check.
     * @return `true` if a use case with the given ID has been requested,
     *         `false` otherwise.
     */
    fun isAttached(id: CameraTargetId) = synchronized(this) {
        useCases[id.ordinal] != null
    }

    /**
     * Attaches a [UseCase] to the pipeline.
     *
     * This method adds the use case to the list of requested use cases and
     * then updates the camera pipeline. The pipeline selects the
     * highest-priority use cases (up to [MAX_CONCURRENT_USE_CASES]) and binds
     * them.
     *
     * For example, if `Preview` and `Record` are attached and `Analyzer` is
     * requested, `Record` will be temporarily detached to make room for
     * `Analyzer` due to its higher priority.
     *
     * It's safe to reattach previously attached useCase without calling
     * [detach] method
     *
     * @param id The [CameraTargetId] for the use case.
     * @param useCase The [UseCase] to attach.
     */
    fun attach(id: CameraTargetId, useCase: UseCase) = synchronized(this) {
        useCases[id.ordinal] = useCase
        updatePipeline()
    }

    /**
     * Detaches a [UseCase] from the pipeline.
     *
     * This method removes the use case from the list of requested use cases
     * and updates the pipeline. If a higher-priority use case is detached,
     * a lower-priority one that was previously excluded might be re-attached.
     *
     * For example, if `Analyzer` is detached, a previously attached `Record`
     * use case may be restored to the pipeline. If no use cases remain, the
     * pipeline is stopped.
     *
     * @param id The [CameraTargetId] of the use case to detach.
     */
    fun detach(id: CameraTargetId) = synchronized(this) {
        useCases[id.ordinal] = null
        updatePipeline()
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
        updatePipeline()
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

    private fun updatePipeline() {
        CameraTargetId.entries
            .filter { useCases[it.ordinal] != null }
            .sortedByDescending(::getTargetPriority)
            .take(MAX_CONCURRENT_USE_CASES)
            .forEachIndexed { i, id ->
                pipelineUseCases[i] = useCases[id.ordinal]
            }

        val pipelineStarted = lifecycle.isStarted()

        when {
            pipelineStarted && pipelineUseCases.all { it == null } -> {
                lifecycle.stop()
            }
            else -> {
                if (!pipelineStarted) {
                    lifecycle.start()
                }

                lifecycle.bindUseCases(
                    selector = selector,
                    useCases = pipelineUseCases,
                )
            }
        }
    }
}

private fun getTargetPriority(id: CameraTargetId): Int = when (id) {
    CameraTargetId.Record -> 0
    CameraTargetId.Analyzer -> 1
    CameraTargetId.Preview -> 2
}

private const val TAG = "CameraSource"

private const val MAX_CONCURRENT_USE_CASES = 2
