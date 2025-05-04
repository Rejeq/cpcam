package com.rejeq.cpcam.core.camera.query

import android.hardware.camera2.CameraCharacteristics
import com.rejeq.cpcam.core.data.model.Framerate

/**
 * Retrieves all supported camera framerate ranges.
 *
 * @param char The camera characteristics containing device capabilities
 * @return List of supported framerate ranges
 */
internal fun querySupportedFramerates(
    char: CameraCharacteristics,
): List<Framerate> {
    val fpsRanges = char.get(
        CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES,
    )
    requireNotNull(fpsRanges) {
        "CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES should exist"
    }

    return fpsRanges.map { Framerate(it.lower, it.upper) }
}
