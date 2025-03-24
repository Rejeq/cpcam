package com.rejeq.cpcam.core.camera

import android.view.Surface
import androidx.camera.core.SurfaceRequest

/**
 * A thin wrapper around CameraX's SurfaceRequest that exposes only essential
 * functionality. This wrapper helps reduce direct dependencies on CameraX in
 * modules that only need basic surface management capabilities.
 */
class SurfaceRequestWrapper(private val request: SurfaceRequest) {
    /**
     * The resolution of the surface request
     *
     * @see SurfaceRequest.getResolution
     */
    val resolution get() = request.resolution

    /**
     * Provides a surface for the camera to render to.
     *
     * @param surface The Surface where camera preview/recording will be
     *        rendered
     * @param executor The executor to run the result callback on
     * @param callback Callback to receive the result of the surface provision
     *
     * @see SurfaceRequest.provideSurface
     */
    fun provideSurface(
        surface: Surface,
        executor: (Runnable) -> Unit,
        callback: (SurfaceRequestResult) -> Unit,
    ) {
        request.provideSurface(surface, executor) {
            callback(SurfaceRequestResult.from(it.resultCode))
        }
    }

    /**
     * Invalidates the current surface request.
     * This should be called when the surface is no longer needed or valid.
     *
     * @return true if the provided Surface is invalidated or
     *         false if it was already invalidated.
     *
     * @see SurfaceRequest.invalidate
     */
    fun invalidate() = request.invalidate()
}

enum class SurfaceRequestResult {
    Unknown,
    UsedSuccessfully,
    Canceled,
    InvalidSurface,
    AlreadyProvided,
    WillNotProvideSurface,
    ;

    companion object {
        fun from(@SurfaceRequest.Result.ResultCode result: Int) =
            when (result) {
                SurfaceRequest.Result.RESULT_INVALID_SURFACE ->
                    InvalidSurface

                SurfaceRequest.Result.RESULT_REQUEST_CANCELLED ->
                    Canceled

                SurfaceRequest.Result.RESULT_SURFACE_ALREADY_PROVIDED ->
                    AlreadyProvided

                SurfaceRequest.Result.RESULT_SURFACE_USED_SUCCESSFULLY ->
                    UsedSuccessfully

                SurfaceRequest.Result.RESULT_WILL_NOT_PROVIDE_SURFACE ->
                    WillNotProvideSurface

                else ->
                    Unknown
            }
    }
}
