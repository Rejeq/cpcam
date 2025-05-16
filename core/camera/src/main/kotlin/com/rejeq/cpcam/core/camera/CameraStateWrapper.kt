package com.rejeq.cpcam.core.camera

import androidx.camera.core.CameraState

data class CameraStateWrapper(val type: CameraType, val error: CameraError?) {
    companion object {
        fun from(state: CameraState) = CameraStateWrapper(
            type = CameraType.from(state.type),
            error = state.error?.let { CameraError.from(it.code) },
        )
    }
}

enum class CameraType {
    Open,
    Close,
    ;

    companion object {
        fun from(type: CameraState.Type) = when (type) {
            CameraState.Type.PENDING_OPEN -> Close
            CameraState.Type.OPENING -> Close
            CameraState.Type.OPEN -> Open
            CameraState.Type.CLOSING -> Open
            CameraState.Type.CLOSED -> Close
        }
    }
}

enum class CameraError {
    Unknown,
    PermissionDenied,
    Fatal,
    InUse,
    Disabled,
    Recoverable,
    Config,
    DoNotDisturbEnabled,
    ;

    companion object {
        fun from(code: Int) = when (code) {
            CameraState.ERROR_CAMERA_IN_USE -> InUse
            CameraState.ERROR_MAX_CAMERAS_IN_USE -> InUse
            CameraState.ERROR_OTHER_RECOVERABLE_ERROR -> Recoverable
            CameraState.ERROR_STREAM_CONFIG -> Config
            CameraState.ERROR_CAMERA_DISABLED -> Disabled
            CameraState.ERROR_CAMERA_FATAL_ERROR -> Fatal
            CameraState.ERROR_DO_NOT_DISTURB_MODE_ENABLED -> DoNotDisturbEnabled
            else -> Unknown
        }
    }
}
