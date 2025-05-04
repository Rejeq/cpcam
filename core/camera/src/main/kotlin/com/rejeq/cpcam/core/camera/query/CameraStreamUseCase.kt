package com.rejeq.cpcam.core.camera.query

import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.params.OutputConfiguration
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * Checks if a specific stream use case is supported by the camera device.
 * So it can be used in [OutputConfiguration.setStreamUseCase]
 *
 * @param char The [CameraCharacteristics] of the camera device.
 * @param useCase The stream use case to check for support.
 * @return `true` if the specified stream use case is supported by the camera.
 *         `false` otherwise.
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
internal fun isStreamUseCaseSupported(
    char: CameraCharacteristics,
    useCase: Long,
): Boolean {
    val available = char.get(
        CameraCharacteristics.SCALER_AVAILABLE_STREAM_USE_CASES,
    )

    return available?.any { it == useCase } == true
}
