package com.rejeq.cpcam.core.camera.operation

import android.util.Log
import androidx.camera.core.CameraControl
import androidx.camera.core.TorchState
import androidx.lifecycle.asFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.guava.await

/**
 * Operation to check if torch is enabled.
 */
class IsTorchEnabledOp : CameraOperation<Flow<Boolean>> {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun CameraOpExecutor.invoke(): Flow<Boolean> =
        source.camera.flatMapLatest {
            it?.cameraInfo?.torchState?.asFlow() ?: flowOf(TorchState.OFF)
        }.map {
            it == TorchState.ON
        }
}

/**
 * Operation to check if camera has flash unit.
 *
 * If this property emits `true`, you can control the flash state using the
 * associated [EnableTorchOp] method.
 *
 * When no camera is currently available, this flow will emit `false`,
 * indicating no flash unit and no flash control.
 */
class HasFlashUnitOp : CameraOperation<Flow<Boolean>> {
    override fun CameraOpExecutor.invoke(): Flow<Boolean> = source.camera.map {
        it?.cameraInfo?.hasFlashUnit() == true
    }
}

/**
 * Operation to enable or disable the camera torch (flashlight).
 *
 * @property state true to enable the torch, false to disable it
 */
class EnableTorchOp(private val state: Boolean) :
    AsyncCameraOperation<TorchError?> {
    override suspend fun CameraOpExecutor.invoke(): TorchError? {
        val control = source.camera.value?.cameraControl

        return when {
            control == null -> TorchError.CameraNotStarted
            else -> tryTorchCall {
                control.enableTorch(state).await()
            }
        }
    }
}

/**
 * Possible errors that can occur during torch operations.
 */
enum class TorchError {
    /** Operation was cancelled */
    Cancelled,

    /** Camera is not started */
    CameraNotStarted,

    /** Torch operation is not in a valid state */
    IllegalState,
}

private inline fun tryTorchCall(block: () -> Unit): TorchError? = try {
    block()
    null
} catch (e: CameraControl.OperationCanceledException) {
    Log.w(TAG, "Torch operation was canceled", e)
    TorchError.Cancelled
} catch (e: IllegalStateException) {
    Log.w(TAG, "Torch operation got illegal state", e)
    TorchError.IllegalState
}

private const val TAG = "CameraTorchOperation"
