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
import java.util.concurrent.ExecutionException
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

    /**
     * Flag to indicate that a stop request has been issued but hasn't yet been
     * executed.
     *
     * This is needed to handle a race condition during configuration changes:
     * when `lifecycle.stop()` is called, the actual stop occurs on the next
     * Looper cycle. During this window, the lifecycle is still technically
     * "started", so any call to `lifecycle.start()` is skipped in
     * [CameraSource], assuming the lifecycle is already active. As a result,
     * the camera may never be restarted.
     *
     * By tracking this intermediate state, we can avoid incorrectly skipping a
     * necessary start call when a stop is pending but not yet applied.
     */
    private var aboutToStop = false

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
    fun isStarted() =
        !aboutToStop && lifecycle.currentState == Lifecycle.State.STARTED

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
        aboutToStop = true
        executor.execute {
            aboutToStop = false
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

    suspend fun getCameraInfo(selector: CameraSelector): CameraInfo? =
        queryDirectProvider { cameraProvider ->
            cameraProvider.getCameraInfo(selector)
        }

    /**
     * Executes [action] with the [ProcessCameraProvider] once it's available.
     *
     * If the provider is already initialized, [action] is run on [executor].
     * Otherwise, a listener is registered, and [action] runs on [executor]
     * when the provider becomes available.
     *
     * Note: [action] may never be invoked if retrieving the provider from
     * [cameraProviderFuture] fails.
     *
     * @param action Lambda receiving the initialized [ProcessCameraProvider].
     */
    private fun queryProvider(
        action: (cameraProvider: ProcessCameraProvider) -> Unit,
    ) {
        cameraProvider?.let {
            executor.execute {
                action(it)
            }

            return
        }

        cameraProviderFuture.addListener({
            cameraProvider = tryCameraProviderCall {
                cameraProviderFuture.get()
            }

            cameraProvider?.let {
                action(it)
            }
        }, executor)
    }

    /**
     * Queries the camera provider directly and executes the
     * provided action on a current thread (direct executor), allowing to
     * return the value from action
     *
     * @param action The action to perform with [ProcessCameraProvider].
     * @return The result of [action] execution
     *         or `null` if retrieving the provider from [cameraProviderFuture]
     *         fails
     */
    private suspend fun <T> queryDirectProvider(
        action: (cameraProvider: ProcessCameraProvider) -> T,
    ): T? {
        cameraProvider?.let {
            return action(it)
        }

        cameraProvider = tryCameraProviderCall {
            cameraProviderFuture.await()
        }

        return cameraProvider?.let {
            action(it)
        }
    }
}

private inline fun <T> tryCameraProviderCall(block: () -> T): T? = try {
    block()
} catch (e: ExecutionException) {
    // ExecutionException called when directly use listener
    Log.e(TAG, "Camera provider initialization failed", e)
    null
} catch (e: Exception) {
    // Any Exception can be thrown when we use guava wrappers
    Log.e(TAG, "Camera provider initialization failed", e)
    null
}

private const val TAG = "CameraLifecycle"
