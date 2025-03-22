package com.rejeq.cpcam.core.camera

import android.annotation.SuppressLint
import android.util.Size
import androidx.camera.core.CameraFilter
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.impl.CameraInfoInternal
import androidx.camera.core.resolutionselector.ResolutionFilter
import androidx.camera.core.resolutionselector.ResolutionSelector
import com.rejeq.cpcam.core.data.model.Resolution

/**
 * Requires a specific camera ID to be used.
 *
 * This extension function filters the available cameras and only allows the
 * camera with the specified ID to be selected.
 *
 * @param id The ID of the camera that is required.
 * @return A [CameraSelector.Builder] with a filter that only allows the
 *         specified camera ID.
 * @throws IllegalArgumentException if the provided [CameraInfo] is not an
 *         instance of [CameraInfoInternal].
 *
 * @sample
 * ```kotlin
 * val cameraSelector = CameraSelector.Builder()
 *     .requireCameraId("0") // Require the camera with ID "0"
 *     .build()
 * ```
 */
@SuppressLint("RestrictedApi")
fun CameraSelector.Builder.requireCameraId(id: String) =
    addCameraFilter(object : CameraFilter {
        override fun filter(infos: List<CameraInfo?>): List<CameraInfo?> =
            infos.filter { info ->
                // This check also in [LensFacingCameraFilter], so I assume its
                // safe
                require(info is CameraInfoInternal) {
                    "The camera info doesn't contain internal implementation."
                }

                info.cameraId == id
            }
    })

/**
 * Requires the camera to use a specific resolution.
 *
 * This function sets a resolution filter that only allows sizes matching the
 * provided [res] resolution. If the camera does not support the exact
 * specified resolution, then no resolution will be available and capture
 * will likely fail.
 *
 * @param res The exact [Resolution] to require.
 * @return The [ResolutionSelector.Builder] instance for chaining.
 */
fun ResolutionSelector.Builder.requireResolution(res: Resolution) =
    setResolutionFilter(object : ResolutionFilter {
        override fun filter(sizes: List<Size>, rotation: Int): List<Size> =
            sizes.filter { size ->
                size.width == res.width && size.height == res.height
            }
    })
