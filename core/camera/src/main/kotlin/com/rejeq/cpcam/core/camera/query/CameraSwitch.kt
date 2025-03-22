package com.rejeq.cpcam.core.camera.query

import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.util.Log
import kotlin.collections.mutableListOf

/**
 * This file provides utility methods for managing camera switching
 * functionality in the Camera2 API.
 */

/**
 * Determines the next camera ID in the switching sequence.
 *
 * The switching sequence follows a specific order:
 * 1. External cameras (if any)
 * 2. Back-facing camera (if available)
 * 3. Front-facing camera (if available)
 *
 * When the last camera in the sequence is reached, it wraps around to the first
 * camera.
 *
 * @param manager The CameraManager instance used to access camera information
 * @param currCamId The current camera ID, or null if no camera is selected
 * @return The next camera ID in the sequence, or null if no cameras are
 *         available
 */
fun queryNextCameraId(manager: CameraManager, currCamId: String?): String? {
    // Not cached, because external cameras can be added at any time
    val switchList = makeCameraSwitchList(manager)
    val currCamIndex = switchList.indexOf(currCamId)

    if (currCamIndex == -1) {
        return switchList.getOrNull(0)
    }

    return switchList.getOrNull((currCamIndex + 1) % switchList.size)
}

/**
 * Creates an ordered list of available camera IDs based on camera facing.
 *
 * Logic based on this [camera enumeration article](https://medium.com/androiddevelopers/camera-enumeration-on-android-9a053b910cb5)
 *
 * @param manager The CameraManager instance used to access camera information
 * @return List of camera IDs in the preferred switching order
 */
private fun makeCameraSwitchList(manager: CameraManager): List<String> {
    val externalIdList = mutableListOf<String>()
    var backId: String? = null
    var frontId: String? = null

    val camList = getCameraIdList(manager) ?: return listOf()

    for (id in camList) {
        val char = getCameraCharacteristics(manager, id)
        if (char == null || !isBackwardCompatible(char)) {
            continue
        }

        val facing = char.get(CameraCharacteristics.LENS_FACING)
        when (facing) {
            CameraMetadata.LENS_FACING_BACK ->
                if (backId == null) {
                    backId = id
                }
            CameraMetadata.LENS_FACING_FRONT ->
                if (frontId == null) {
                    frontId = id
                }
            CameraMetadata.LENS_FACING_EXTERNAL ->
                externalIdList.add(id)
        }
    }

    return (externalIdList + listOf(backId, frontId)).filterNotNull()
}

private fun getCameraIdList(manager: CameraManager): Array<String>? = try {
    manager.cameraIdList
} catch (e: CameraAccessException) {
    Log.w(TAG, "Unable to list cameras: ${e.message}")
    null
}

private fun getCameraCharacteristics(
    manager: CameraManager,
    id: String,
): CameraCharacteristics? = try {
    manager.getCameraCharacteristics(id)
} catch (e: IllegalArgumentException) {
    Log.w(TAG, "Unable to get camera info '$id': ${e.message}")
    null
} catch (e: CameraAccessException) {
    Log.w(TAG, "Unable to get camera info '$id': ${e.message}")
    null
}

private fun isBackwardCompatible(char: CameraCharacteristics): Boolean {
    val capabilities =
        char.get(
            CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES,
        )

    return capabilities?.contains(
        CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_BACKWARD_COMPATIBLE,
    ) == true
}

private const val TAG = "CameraSwitch"
