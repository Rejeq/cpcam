package com.rejeq.cpcam.core.camera.operation

import android.util.Log
import com.rejeq.cpcam.core.camera.query.querySupportedFramerates
import com.rejeq.cpcam.core.data.model.Framerate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Operation to get supported framerates.
 *
 * This function retrieves the supported framerates from the underlying
 * camera source and emits them as a Flow. The Flow will emit a new list
 * whenever the camera source changes.
 *
 * @return A Flow emitting a list of [Framerate] objects representing the
 *        supported framerate ranges.
 *        If no framerates are supported, the Flow will emit an empty list.
 *        The flow will emit a new value every time the camera changes.
 */
class GetSupportedFrameratesOp : CameraOperation<Flow<List<Framerate>>> {
    override fun CameraOpExecutor.invoke(): Flow<List<Framerate>> =
        source.camera.map {
            GetCurrentSupportedFrameratesOp().invoke()
        }
}

/**
 * Operation to get current supported framerates.
 *
 * @return A list of [Framerate] objects representing the supported
 *         framerate ranges.
 *         If the camera is not available or its information cannot be
 *         accessed, an empty list is returned.
 *
 * @see querySupportedFramerates
 */
class GetCurrentSupportedFrameratesOp : CameraOperation<List<Framerate>> {
    override fun CameraOpExecutor.invoke(): List<Framerate> {
        val char = source.getCameraCharacteristics()
        if (char == null) {
            Log.e(
                TAG,
                "Unable to query supported framerates: Unknown characteristics",
            )
            return emptyList()
        }

        return querySupportedFramerates(char)
    }
}

private const val TAG = "CameraFramerateOp"
