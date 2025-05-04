package com.rejeq.cpcam.core.camera.operation

import android.util.Log
import com.rejeq.cpcam.core.camera.query.queryNextCameraId

/**
 * Handles switching between available cameras.
 *
 * This operation queries for the next available camera ID based on the current
 * camera and switches to it if found.
 * If no other camera is available, it will silently do nothing.
 */
class CameraSwitchOp : CameraOperation<Unit> {
    override fun CameraOpExecutor.invoke() {
        Log.i(TAG, "Switching camera")

        queryNextCameraId(source.manager, source.currId)?.let { newId ->
            source.setCameraId(newId)
        }
    }
}

private const val TAG = "CameraSwitchOperation"
