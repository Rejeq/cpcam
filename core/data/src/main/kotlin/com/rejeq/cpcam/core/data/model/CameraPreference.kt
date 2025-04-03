package com.rejeq.cpcam.core.data.model

/**
 * Domain model representing camera-specific preferences.
 *
 * @property resolution Preferred resolution for the camera
 * @property framerate Preferred framerate for the camera
 */
data class CameraPreference(
    val resolution: Resolution? = null,
    val framerate: Framerate? = null,
)
