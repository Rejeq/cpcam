package com.rejeq.cpcam.feature.main.camera

import androidx.compose.runtime.Immutable
import com.rejeq.cpcam.core.camera.CameraError
import com.rejeq.cpcam.core.camera.CameraStateWrapper
import com.rejeq.cpcam.core.camera.CameraType
import com.rejeq.cpcam.core.camera.SurfaceRequestWrapper
import com.rejeq.cpcam.core.camera.target.SurfaceRequestState

/**
 * Represents the various states of the camera preview lifecycle.
 *
 * All states must provide a [request], even when the camera is not fully
 * opened. This ensures that the last available camera frame can still be
 * displayed during transitions (e.g., when the camera is opening or has just
 * closed), helping to prevent flickering or blank frames during state changes.
 *
 * By keeping the latest [SurfaceRequestWrapper] accessible in all states,
 * the UI can continue showing the most recent image until the new preview is
 * ready.
 */
sealed interface CameraPreviewState {
    val request: SurfaceRequestWrapper?

    data class Failed(val error: CameraError) : CameraPreviewState {
        override val request: SurfaceRequestWrapper? = null
    }

    @Immutable
    data class Closed(override val request: SurfaceRequestWrapper? = null) :
        CameraPreviewState

    @Immutable
    data class Opened(override val request: SurfaceRequestWrapper) :
        CameraPreviewState
}

fun CameraStateWrapper.fromDomain(
    requestState: SurfaceRequestState,
): CameraPreviewState {
    error?.let { return CameraPreviewState.Failed(it) }

    val request = (requestState as? SurfaceRequestState.Available)?.value
    return when (type) {
        CameraType.Close -> CameraPreviewState.Closed(request)
        CameraType.Open -> {
            if (request != null) {
                CameraPreviewState.Opened(request)
            } else {
                CameraPreviewState.Closed(request)
            }
        }
    }
}
