package com.rejeq.cpcam.core.camera.target

import androidx.camera.core.Preview
import com.rejeq.cpcam.core.camera.CameraTargetId
import com.rejeq.cpcam.core.camera.source.CameraSource
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Provides a surface for the camera preview.
 *
 * It manages the lifecycle of a [Preview] use case from CameraX, exposing the
 * [surfaceRequest] to be used by a view for rendering.
 *
 * @property source The [CameraSource] instance responsible for managing the
 *           camera's lifecycle and attaching/detaching use cases.
 */
@Singleton
class PreviewCameraTarget @Inject constructor(
    private val source: CameraSource,
) : CameraTarget {
    private val targetId = CameraTargetId.Preview

    private val _surfaceRequest =
        MutableStateFlow<SurfaceRequestState>(SurfaceRequestState.Stopped)
    override val surfaceRequest = _surfaceRequest.asStateFlow()

    private val useCase = Preview.Builder().build().apply {
        setSurfaceProvider { newSurfaceRequest ->
            _surfaceRequest.value =
                SurfaceRequestState.Available(newSurfaceRequest)
        }
    }

    override fun start() {
        source.attach(targetId, useCase)
    }

    override fun stop() {
        source.detach(targetId)
        _surfaceRequest.value = SurfaceRequestState.Stopped
    }
}
