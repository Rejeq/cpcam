package com.rejeq.cpcam.core.camera.operation

import android.util.Log
import androidx.camera.core.CameraControl
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.MeteringPoint
import com.rejeq.cpcam.core.camera.SurfaceRequestWrapper
import com.rejeq.cpcam.core.camera.target.CameraTarget
import kotlinx.coroutines.guava.await

/**
 * Operation to set the camera focus point relative a specific target.
 *
 * @property x The x-coordinate of the point
 * @property y The y-coordinate of the point
 * @property target The target for which to set the focus point
 */
class SetFocusPointForTargetOp(
    val x: Float,
    val y: Float,
    val target: CameraTarget<SurfaceRequestWrapper>,
) : AsyncCameraOperation<FocusError?> {
    override suspend fun CameraOpExecutor.invoke(): FocusError? {
        val point = target.getPoint(x, y)
        if (point == null) {
            Log.w(TAG, "Failed to set focus: Unable to get point")
            return FocusError.FocusFailed
        }

        return SetFocusPointOp(point).invoke()
    }
}

/**
 * Operation to set the camera focus point.
 *
 * @property point The metering point where to focus
 */
class SetFocusPointOp(private val point: MeteringPoint) :
    AsyncCameraOperation<FocusError?> {
    override suspend fun CameraOpExecutor.invoke(): FocusError? {
        val control = source.camera.value?.cameraControl

        return when {
            control == null -> FocusError.CameraNotStarted
            else -> tryFocusCall {
                val action = FocusMeteringAction.Builder(point).build()

                val res = control.startFocusAndMetering(action).await()
                when (res?.isFocusSuccessful) {
                    true -> null
                    else -> FocusError.FocusFailed
                }
            }
        }
    }
}

/**
 * Possible errors that can occur during focus operations.
 */
enum class FocusError {
    /** Operation was cancelled */
    Cancelled,

    /** Camera is not started */
    CameraNotStarted,

    /** Focus operation is not supported */
    NotSupported,

    /** Focus operation failed */
    FocusFailed,
}

private inline fun tryFocusCall(block: () -> Unit): FocusError? = try {
    block()
    null
} catch (e: CameraControl.OperationCanceledException) {
    Log.w(TAG, "Focus operation was canceled", e)
    FocusError.Cancelled
} catch (e: IllegalArgumentException) {
    Log.w(TAG, "Focus operation not supported", e)
    FocusError.NotSupported
}

private const val TAG = "CameraFocusOperation"
