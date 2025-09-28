package com.rejeq.cpcam.core.camera.target

import androidx.camera.core.Preview
import com.rejeq.cpcam.core.camera.CameraTargetId
import com.rejeq.cpcam.core.camera.SurfaceRequestWrapper
import com.rejeq.cpcam.core.camera.source.CameraSource
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.asStateFlow

/**
 * Provides a surface for the camera preview.
 *
 * @property source The [CameraSource] instance responsible for managing the
 *           camera's lifecycle and attaching/detaching use cases.
 */
@Singleton
class PreviewCameraTarget @Inject constructor(
    private val source: CameraSource,
) {
    private val targetId = CameraTargetId.Preview

    fun start(): CameraRequestFlow<SurfaceRequestWrapper> {
        val request = MutableCameraRequestFlow<SurfaceRequestWrapper>(
            CameraRequestState.Stopped,
        )
        val useCase = buildUseCase(request)

        source.attach(targetId, useCase)
        return request.asStateFlow()
    }

    fun stop() {
        source.detach(targetId)
    }

    private fun buildUseCase(
        request: MutableCameraRequestFlow<SurfaceRequestWrapper>,
    ): Preview = Preview.Builder().build().apply {
        configureSurfaceProvider(request, Runnable::run)
    }
}
