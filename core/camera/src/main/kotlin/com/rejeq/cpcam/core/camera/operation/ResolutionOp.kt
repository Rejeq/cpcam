package com.rejeq.cpcam.core.camera.operation

import android.graphics.ImageFormat
import android.util.Log
import com.rejeq.cpcam.core.camera.query.queryMaxRecordSize
import com.rejeq.cpcam.core.camera.query.querySupportedSizes
import com.rejeq.cpcam.core.data.model.Resolution
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

private const val TAG = "CameraResOperation"
