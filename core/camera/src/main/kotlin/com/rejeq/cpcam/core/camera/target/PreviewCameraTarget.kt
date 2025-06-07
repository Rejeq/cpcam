package com.rejeq.cpcam.core.camera.target

import androidx.camera.core.MeteringPoint
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import com.rejeq.cpcam.core.camera.CameraTargetId
import com.rejeq.cpcam.core.camera.SurfaceRequestWrapper
import com.rejeq.cpcam.core.camera.source.CameraSource
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Provides a surface for the camera preview.
 *
 * It manages the lifecycle of a [Preview] use case from CameraX, exposing the
 * [request] to be used by a view for rendering.
 *
 * @property source The [CameraSource] instance responsible for managing the
 *           camera's lifecycle and attaching/detaching use cases.
 */
@Singleton
class PreviewCameraTarget @Inject constructor(
    private val source: CameraSource,
) : CameraTarget<SurfaceRequestWrapper> {
    private val targetId = CameraTargetId.Preview

    private var meteringPointFactory: SurfaceOrientedMeteringPointFactory? =
        null

    private val _request =
        MutableStateFlow<CameraRequestState<SurfaceRequestWrapper>>(
            CameraRequestState.Stopped,
        )
    override val request = _request.asStateFlow()

    private val useCase = Preview.Builder().build().apply {
        setSurfaceProvider { newSurfaceRequest ->
            _request.value = CameraRequestState.Available(
                SurfaceRequestWrapper(newSurfaceRequest),
            )

            meteringPointFactory = SurfaceOrientedMeteringPointFactory(
                newSurfaceRequest.resolution.width.toFloat(),
                newSurfaceRequest.resolution.height.toFloat(),
            )
        }
    }

    override fun start() {
        source.attach(targetId, useCase)
    }

    override fun stop() {
        source.detach(targetId)
        _request.value = CameraRequestState.Stopped
    }

    override fun getPoint(x: Float, y: Float): MeteringPoint? =
        meteringPointFactory?.createPoint(x, y)
}
