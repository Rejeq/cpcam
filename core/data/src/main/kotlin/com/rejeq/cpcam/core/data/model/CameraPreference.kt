package com.rejeq.cpcam.core.data.model

import android.util.Range

/**
 * Domain model representing camera-specific preferences.
 *
 * @property resolution Preferred resolution for the camera
 * @property framerate Preferred framerate for the camera
 */
data class CameraPreference(
    val resolution: Resolution? = null,
    val framerate: Range<Int>? = null,
)
