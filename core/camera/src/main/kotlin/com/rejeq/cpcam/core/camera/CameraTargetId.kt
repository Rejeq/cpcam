package com.rejeq.cpcam.core.camera

/**
 * Enum class representing the target ID for camera operations.
 *
 * This enum defines the possible targets for actions such as starting a preview
 * or recording video. Each target corresponds to a specific output stream or
 * use case of the camera.
 */
enum class CameraTargetId {
    Preview,
    Record,
    Analyzer,
}
