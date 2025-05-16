package com.rejeq.cpcam.core.camera.operation

import android.graphics.ImageFormat
import android.os.Build
import android.util.Log
import com.rejeq.cpcam.core.camera.query.queryMaxPreviewSize
import com.rejeq.cpcam.core.camera.query.queryMaxRecordSize
import com.rejeq.cpcam.core.camera.query.querySupportedSizes
import com.rejeq.cpcam.core.data.model.Resolution
import com.rejeq.cpcam.core.data.model.long
import com.rejeq.cpcam.core.data.model.short
import kotlin.math.abs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Operation to get supported resolutions for a specific format.
 *
 * This function retrieves the supported resolutions from the underlying
 * camera source and emits them as a Flow. The Flow will emit a new list
 * whenever the camera source changes.
 *
 * @param format The desired image format (e.g., ImageFormat.JPEG,
 *        ImageFormat.YUV_420_888).
 *        This format determines which stream configuration map will be
 *        queried. Must be a valid ImageFormat constant.
 * @return A Flow emitting a list of [Resolution] objects representing the
 *        supported resolutions for the specified format.
 *        If no resolutions are supported for the given format, the Flow
 *        will emit an empty list.
 *        The flow will emit a new value every time the camera changes.
 *
 * @property format The image format to get resolutions for
 */
class GetSupportedResolutionsOp(private val format: Int) :
    CameraOperation<Flow<List<Resolution>>> {
    override fun CameraOpExecutor.invoke(): Flow<List<Resolution>> =
        source.camera.map {
            GetCurrentSupportedResolutionsOp(format).invoke()
        }
}

/**
 * Operation to get current supported resolutions for a specific format.
 *
 * @param format The desired image format (e.g., ImageFormat.JPEG,
 *        ImageFormat.YUV_420_888).
 *        This format determines which stream configuration map will be
 *        queried. Must be a valid ImageFormat constant.
 * @return A list of [Resolution] objects representing the
 *         supported resolutions.
 *         If the camera is not available or its information cannot be
 *         accessed, an empty list is emitted.
 *
 * @see ImageFormat
 * @see querySupportedSizes
 */
class GetCurrentSupportedResolutionsOp(private val format: Int) :
    CameraOperation<List<Resolution>> {
    override fun CameraOpExecutor.invoke(): List<Resolution> {
        val char = source.getCameraCharacteristics()
        if (char == null) {
            Log.e(
                TAG,
                "Unable to query supported resolutions: " +
                    "Unknown characteristics",
            )
            return emptyList()
        }

        return querySupportedSizes(char, format)
    }
}

/**
 * Operation to get record resolutions for a specific format.
 *
 * @property format The image format to get resolutions for
 *
 * @see ImageFormat
 */
class GetRecordResolutionsOp(private val format: Int) :
    CameraOperation<Flow<List<Resolution>>> {
    override fun CameraOpExecutor.invoke(): Flow<List<Resolution>> =
        source.camera.map {
            GetCurrentRecordResolutionsOp(format).invoke()
        }
}

/**
 * Operation to get current record resolutions for a specific format.
 *
 * @property format The image format to get resolutions for
 *
 * @see ImageFormat
 */
class GetCurrentRecordResolutionsOp(private val format: Int) :
    CameraOperation<List<Resolution>> {
    override fun CameraOpExecutor.invoke(): List<Resolution> {
        val char = source.getCameraCharacteristics()
        val camId = source.currId

        return when {
            char == null -> {
                Log.e(
                    TAG,
                    "Unable to query supported resolutions: " +
                        "Unknown characteristics",
                )

                emptyList()
            }
            camId == null -> {
                Log.e(
                    TAG,
                    "Unable to query supported resolutions: " +
                        "Unknown camera id",
                )

                emptyList()
            }
            else -> {
                val max = queryMaxRecordSize(char, camId, format)
                if (max == null) {
                    Log.e(
                        TAG,
                        "Unable to query supported resolutions: " +
                            "Unknown maximum record size",
                    )
                    return emptyList()
                }

                querySupportedSizes(char, format).filter { it < max }
            }
        }
    }
}

/**
 * Operation to get current preview resolutions.
 *
 * Preview resolution refers to the best size match to the device's screen
 * resolution, or to 1080p (1920x1080), whichever is smaller.
 *
 * @property windowSize The size of the window to get the resolutions for
 */
class GetCurrentPreviewResolutionsOp(private val windowSize: Resolution) :
    CameraOperation<List<Resolution>> {
    override fun CameraOpExecutor.invoke(): List<Resolution> {
        val char = source.getCameraCharacteristics()

        return when {
            char == null -> {
                Log.e(
                    TAG,
                    "Unable to query supported resolutions: " +
                        "Unknown characteristics",
                )

                emptyList()
            }
            else -> {
                // FIXME: This is a hack, guess proper format for older phones
                val format =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        ImageFormat.PRIVATE
                    } else {
                        ImageFormat.YUV_420_888
                    }

                val max = queryMaxPreviewSize(char, format, windowSize)
                if (max == null) {
                    Log.e(
                        TAG,
                        "Unable to query supported resolutions: " +
                            "Unknown maximum record size",
                    )
                    return emptyList()
                }

                querySupportedSizes(char, format).filter { it < max }
            }
        }
    }
}

/**
 * Operation to get the best preview resolution for a specific window size.
 *
 * This function retrieves the supported resolutions from the underlying
 * camera source and emits them as a Flow. The Flow will emit a new list
 * whenever the camera source changes.
 *
 * @see GetCurrentBestPreviewResolutionOp
 */
class GetBestPreviewResolutionsOp(private val windowSize: Resolution) :
    CameraOperation<Flow<Resolution?>> {
    override fun CameraOpExecutor.invoke(): Flow<Resolution?> =
        source.camera.map {
            GetCurrentBestPreviewResolutionOp(windowSize).invoke()
        }
}

/**
 * Operation to get the best preview resolution for a specific window size.
 *
 * It tries to find a resolution with an aspect ratio closest to the window's
 * aspect ratio.
 *
 * @property winSize The size of the window to get the best resolution for.
 * @return The best matching [Resolution] for preview, or null if no suitable
 *         resolution is found. The returned resolution will match the closest
 *         aspect ratio to the window
 */
class GetCurrentBestPreviewResolutionOp(private val winSize: Resolution) :
    CameraOperation<Resolution?> {
    override fun CameraOpExecutor.invoke(): Resolution? {
        Log.d(TAG, "Getting best preview resolution for $winSize resolution")

        val available = GetCurrentPreviewResolutionsOp(winSize).invoke()
        if (available.isEmpty()) {
            Log.w(
                TAG,
                "Unable to get best preview resolution: " +
                    "No resolutions available",
            )

            return null
        }

        val isLandscapeRatio = available.first().aspectRatio > 1.0f
        val targetAspectRatio: Float = if (isLandscapeRatio) {
            winSize.long() / winSize.short().toFloat()
        } else {
            winSize.short() / winSize.long().toFloat()
        }

        Log.d(TAG, "Target aspect ratio: $targetAspectRatio")
        val closest = available.minBy { resolution ->
            abs(resolution.aspectRatio - targetAspectRatio)
        }

        Log.i(TAG, "Best preview resolution for $winSize window is $closest")
        return closest
    }
}

private const val TAG = "CameraResOperation"
