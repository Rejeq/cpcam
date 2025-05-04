package com.rejeq.cpcam.core.camera.operation

import android.Manifest
import android.content.Context
import android.util.Log
import androidx.camera.core.CameraState
import androidx.lifecycle.asFlow
import com.rejeq.cpcam.core.camera.CameraError
import com.rejeq.cpcam.core.camera.CameraStateWrapper
import com.rejeq.cpcam.core.camera.CameraType
import com.rejeq.cpcam.core.common.hasPermission
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

/**
 * Provides a flow of camera state.
 */
class CameraStateOp : CameraOperation<Flow<CameraStateWrapper>> {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun CameraOpExecutor.invoke(): Flow<CameraStateWrapper> =
        source.cameraInfo
            .flatMapLatest { it.cameraState.asFlow() }
            .map { mapToWrapper(it, context) }

    private fun mapToWrapper(
        rawState: CameraState,
        context: Context,
    ): CameraStateWrapper {
        if (!context.hasPermission(Manifest.permission.CAMERA)) {
            return CameraStateWrapper(
                type = CameraType.Close,
                error = CameraError.PermissionDenied,
            )
        }

        val wrapper = CameraStateWrapper.from(rawState)
        wrapper.error?.let { error ->
            Log.i(TAG, "Camera error occurred: $error")
        }

        return wrapper
    }
}

/**
 * Operation to get the flow of the active camera ID.
 *
 * The flow will emit new id whenever the underlying camera
 * source changes, indicating a switch to a different camera.
 */
class GetCameraIdOp : CameraOperation<Flow<String?>> {
    override fun CameraOpExecutor.invoke(): Flow<String?> = source.camera.map {
        GetCurrentCameraIdOp().invoke()
    }
}

/**
 * Operation to get the current camera ID.
 */
class GetCurrentCameraIdOp : CameraOperation<String?> {
    override fun CameraOpExecutor.invoke(): String? = source.currId
}

private const val TAG = "CameraStateOperation"
