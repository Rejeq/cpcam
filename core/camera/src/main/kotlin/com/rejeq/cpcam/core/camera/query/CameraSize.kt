package com.rejeq.cpcam.core.camera.query

import android.annotation.SuppressLint
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3
import android.hardware.camera2.CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL
import android.hardware.camera2.CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL
import android.hardware.camera2.CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY
import android.hardware.camera2.CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED
import android.media.CamcorderProfile
import android.media.CamcorderProfile.QUALITY_HIGH
import android.media.CamcorderProfile.QUALITY_LOW
import com.rejeq.cpcam.core.data.model.Resolution
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * This file provides utility methods for handling camera resolution and
 * size-related operations in the Camera2 API.
 */

/**
 * Determines the maximum supported preview size for the camera.
 *
 * The preview size is determined by comparing the window size with Full HD
 * resolution ([FullHdSize]). If the window size is smaller than Full HD in both
 * dimensions, the window size is used as the target size. Otherwise, Full HD is
 * used as the target size.
 *
 * Logic based on this [camera2 documentation](https://developer.android.com/reference/android/hardware/camera2/CameraDevice#createCaptureSession(android.hardware.camera2.params.SessionConfiguration))
 *
 * @param char The camera characteristics containing device capabilities
 * @param format The image format (e.g., ImageFormat.JPEG,
 *        ImageFormat.YUV_420_888)
 * @param winSize Optional window size to consider when determining preview size
 * @return The optimal preview size that fits within the target dimensions, or
 *         null if no suitable size is found
 */
fun queryMaxPreviewSize(
    char: CameraCharacteristics,
    format: Int,
    winSize: Resolution? = null,
): Resolution? {
    val sizes = querySupportedSizes(char, format)

    val smaller = if (winSize != null &&
        winSize.long() < FullHdSize.long() &&
        winSize.short() < FullHdSize.short()
    ) {
        winSize
    } else {
        FullHdSize
    }

    return sizes.firstOrNull {
        it.long() <= smaller.long() && it.short() <= smaller.short()
    }
}

/**
 * Determines the maximum supported recording size based on the device's
 * hardware level capabilities. It assumes that other targets (Preview) also
 * requested
 *
 * The selection logic varies based on the camera's hardware support level:
 * - FULL: Returns the MAXIMUM supported size
 * - EXTERNAL: Returns the RECORD size from [getCamcorderSize]
 * - Others (LEGACY): Returns MAXIMUM size for JPEG format, or PREVIEW size for
 *   other formats
 *
 * Logic based on this [hardware level capabilities table](https://developer.android.com/reference/android/hardware/camera2/CameraDevice#createCaptureSession(android.hardware.camera2.params.SessionConfiguration))
 *
 * @param char The camera characteristics containing device capabilities
 * @param camId The camera ID string of the characteristics
 * @param format The image format (e.g., ImageFormat.JPEG,
 *        ImageFormat.YUV_420_888)
 * @throws IllegalArgumentException if INFO_SUPPORTED_HARDWARE_LEVEL is not
 *         available
 * @return The maximum supported recording size, or null if no suitable size is
 *         found
 */
fun queryMaxRecordSize(
    char: CameraCharacteristics,
    camId: String,
    format: Int,
): Resolution? {
    val level = char.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
    requireNotNull(level) { "INFO_SUPPORTED_HARDWARE_LEVEL should exist" }

    return when {
        isLevelSupported(level, INFO_SUPPORTED_HARDWARE_LEVEL_FULL) -> {
            // Return MAXIMUM size
            querySupportedSizes(char, format).getOrNull(0)
        }
        isLevelSupported(level, INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL) -> {
            // Return RECORD size
            val size =
                camId.toIntOrNull()?.let { id ->
                    getCamcorderSize(id)
                }

            size
        }
        else -> { // Assume we are in LEGACY hardware level
            if (format == ImageFormat.JPEG) {
                // Return MAXIMUM size
                querySupportedSizes(char, format).getOrNull(0)
            } else {
                // Return PREVIEW size
                queryMaxPreviewSize(char, format)
            }
        }
    }
}

/**
 * Determines the default recording size that's suitable for most use cases.
 *
 * Returns a size that's smaller than or equal to both the maximum supported
 * size and HD resolution ([HdSize]).
 *
 * @param char The camera characteristics containing device capabilities
 * @param camId The camera ID string of the characteristics
 * @param format The image format (e.g., ImageFormat.JPEG,
 *        ImageFormat.YUV_420_888)
 * @return The default recording size, or null if no suitable size is found
 * @see queryMaxRecordSize
 */
fun queryDefaultRecordSize(
    char: CameraCharacteristics,
    camId: String,
    format: Int,
): Resolution? {
    val max = queryMaxRecordSize(char, camId, format) ?: return null
    val sizes = querySupportedSizes(char, format)

    // TODO: Add support for different aspect ratios, currently uses only 16:9
    val hdSize = HdSize.width * HdSize.height
    val maxSize = max.width * max.height

    return sizes.firstOrNull {
        val size = it.width * it.height

        size <= maxSize && size <= hdSize
    }
}

/**
 * Retrieves all supported camera sizes for the specified format in decreasing
 * order.
 *
 * @param char The camera characteristics containing device capabilities
 * @param format The image format (e.g., ImageFormat.JPEG,
 *        ImageFormat.YUV_420_888)
 * @return List of supported sizes sorted by area in descending order
 * @throws IllegalArgumentException if SCALER_STREAM_CONFIGURATION_MAP is not
 *         available
 * @see ImageFormat
 */
fun querySupportedSizes(
    char: CameraCharacteristics,
    format: Int,
): List<Resolution> {
    val config = char.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
    requireNotNull(config) { "SCALER_STREAM_CONFIGURATION_MAP should exist" }
    assert(config.isOutputSupportedFor(format))

    // NOTE: getOutputSizes(Class<T>) behave same as
    // getOutputSizes(ImageFormat.Private)
    return config.getOutputSizes(format).sortedByDescending {
        abs(it.width * it.height)
    }.map { Resolution(it.width, it.height) }
}

/**
 * Checks if the device's hardware level meets or exceeds the required level.
 *
 * The comparison follows the Camera2 API hardware level hierarchy:
 * LEGACY < EXTERNAL < LIMITED < FULL < LEVEL_3
 *
 * Logic based on this [camera2 documentation](https://developer.android.com/reference/android/hardware/camera2/CameraCharacteristics#INFO_SUPPORTED_HARDWARE_LEVEL)
 *
 * @param deviceLevel The current device's hardware level
 * @param requiredLevel The minimum required hardware level
 * @return true if the device meets or exceeds the required level, false
 *         otherwise
 */
@SuppressLint("InlinedApi")
private fun isLevelSupported(deviceLevel: Int, requiredLevel: Int): Boolean {
    if (requiredLevel == deviceLevel) {
        return true
    }

    val sortedLevels = listOf(
        INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY,
        INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL,
        INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED,
        INFO_SUPPORTED_HARDWARE_LEVEL_FULL,
        INFO_SUPPORTED_HARDWARE_LEVEL_3,
    )

    val first = sortedLevels.firstOrNull {
        it == requiredLevel || it == deviceLevel
    }

    return first == requiredLevel
}

/**
 * Retrieves the camera size from CamcorderProfile for the specified camera ID.
 *
 * Attempts to get the size from HIGH quality profile first, falls back to LOW
 * quality if HIGH is not available.
 *
 * @param id The camera ID as an integer
 * @return The size from CamcorderProfile, or null if no profile is available
 */
private fun getCamcorderSize(id: Int): Resolution? {
    val profile = when {
        CamcorderProfile.hasProfile(QUALITY_HIGH) -> {
            CamcorderProfile.get(id, QUALITY_HIGH)
        }
        CamcorderProfile.hasProfile(QUALITY_LOW) -> {
            CamcorderProfile.get(id, QUALITY_LOW)
        }
        else -> {
            return null
        }
    }

    return Resolution(profile.videoFrameWidth, profile.videoFrameHeight)
}

/** Returns the longer dimension of the size (maximum of width and height). */
private fun Resolution.long() = max(this.width, this.height)

/** Returns the shorter dimension of the size (minimum of width and height). */
private fun Resolution.short() = min(this.width, this.height)

/** Default Full HD (1920x1080) resolution */
private val FullHdSize = Resolution(1920, 1080)

/** Default HD (1280x720) resolution */
private val HdSize = Resolution(1280, 720)
