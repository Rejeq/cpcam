package com.rejeq.cpcam.core.camera.source

import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.UseCase
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.google.common.util.concurrent.ListenableFuture
import com.rejeq.cpcam.core.camera.di.MainExecutor
import java.util.concurrent.Executor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.guava.await

class CameraLifecycle(
    private val cameraProviderFuture: ListenableFuture<ProcessCameraProvider>,
    @MainExecutor private val executor: Executor,
) : LifecycleOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry

    private var cameraProvider: ProcessCameraProvider? = null

    private val _camera = MutableStateFlow<Camera?>(null)
    val camera = _camera.asStateFlow()

    /**
     * Checks if the associated lifecycle is in the [Lifecycle.State.STARTED]
     * state.
     *
     * @return `true` if the lifecycle is in the [Lifecycle.State.STARTED]
     *         state, `false` otherwise.
     */
    fun isStarted() = lifecycle.currentState == Lifecycle.State.STARTED

    /**
     * Starts the camera pipeline.
     *
     * This function allows the camera to begin capturing and processing frames.
     *
     * Subsequent calls to `start()` will have no effect.
     *
     * Note: This function executes its actions asynchronously on a separate
     *       thread managed by the [mainExecutor].
     */
    fun start() {
        executor.execute {
            lifecycleRegistry.currentState = Lifecycle.State.STARTED
        }
    }

    /**
     * Stops the camera pipeline. It is important to call this method when the
     * camera is no longer needed.
     *
     * This method is typically invoked when there are no active use cases
     * associated with the camera, so the camera backend is allowed to release
     * its underlying resources.
     *
     * If there attached [UseCase]s in the pipeline, they will stop producing
     * frames
     *
     * Note: This function executes its actions asynchronously on a separate
     *       thread managed by the [mainExecutor].
     */
    fun stop() {
        executor.execute {
            lifecycleRegistry.currentState = Lifecycle.State.CREATED
        }
    }

    /**
     * Binds a set of [UseCase]s to the camera lifecycle. It is not launch
     * pipeline by itself, you must additionally call [start] method
     *
     * It unbinds any existing use cases from the camera provider and then
     * attempts to bind the provided use cases to the given [CameraSelector].
     *
     * Note: This operation is asynchronous. It will query the camera provider
     *       and only then execute logic.
     *
     * @param selector The [CameraSelector] specifying which camera to use for
     *                 the use cases.
     * @param useCases A list of [UseCase]s to be attached to the pipeline
     */
    fun bindUseCases(selector: CameraSelector, vararg useCases: UseCase?) {
        queryProvider { cameraProvider ->
            try {
                cameraProvider.unbindAll()

                if (useCases.isNotEmpty()) {
                    _camera.value = cameraProvider.bindToLifecycle(
                        lifecycleOwner = this,
                        cameraSelector = selector,
                        useCases = useCases.filterNotNull().toTypedArray(),
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Use case binding failed", e)
            }
        }
    }

    /**
     * Unbinds a given [UseCase] from the pipeline.
     *
     * Note: This operation is asynchronous. It will query the camera provider
     *       and then execute the logic.
     *
     * @param useCase The [UseCase] to unbind.
     */
    fun unbindUseCase(useCase: UseCase) {
        queryProvider { cameraProvider ->
            cameraProvider.unbind(useCase)
        }
    }

    /**
     * Unbinds all use cases from the camera provider.
     *
     * This function removes all previously bound use cases from the camera
     * provider. It effectively stops all camera operations and releases the
     * associated resources. This is typically called when the camera is no
     * longer needed, such as when a user navigates away from the camera screen
     * or when the application is closing.
     *
     * Note: This operation is asynchronous. It will query the camera provider
     *       and then execute the logic.
     */
    fun unbindAll() {
        queryProvider { cameraProvider ->
            _camera.value = null
            cameraProvider.unbindAll()
        }
    }

    suspend fun getCameraInfo(selector: CameraSelector): CameraInfo =
        queryDirectProvider { cameraProvider ->
            cameraProvider.getCameraInfo(selector)
        }

    /**
     * Queries the camera provider and executes the given action when the
     * provider is available.
     *
     * If the provider is already available, it executes the action on the
     * specified executor.
     * If the provider is not yet available, it adds a listener to the
     * to be notified when the provider becomes available.
     *
     * @param action The action to be executed with the camera provider.
     *        Always executed in specified [executor]
     */
    private fun queryProvider(
        action: (cameraProvider: ProcessCameraProvider) -> Unit,
    ) {
        if (cameraProvider != null) {
            executor.execute {
                action(cameraProvider!!)
            }
        } else {
            cameraProviderFuture.addListener({
                cameraProvider = cameraProviderFuture.get()
                action(cameraProvider!!)
            }, executor)
        }
    }

    /**
     * Queries the camera provider directly and executes the
     * provided action on a current thread (direct executor), allowing to
     * return the value from action
     *
     * @param action The action to perform on the [ProcessCameraProvider].
     *        This is a lambda function that takes the
     */
    private suspend fun <T> queryDirectProvider(
        action: (cameraProvider: ProcessCameraProvider) -> T,
    ): T = if (cameraProvider != null) {
        action(cameraProvider!!)
    } else {
        cameraProvider = cameraProviderFuture.await()

        action(cameraProvider!!)
    }
}

private const val TAG = "CameraLifecycle"
