package com.rejeq.cpcam.core.stream.target

import android.util.Log
import com.rejeq.cpcam.core.camera.SurfaceRequestWrapper
import com.rejeq.cpcam.core.camera.target.RecordCameraTarget
import com.rejeq.cpcam.core.camera.target.SurfaceRequestState
import com.rejeq.cpcam.core.common.di.ApplicationScope
import com.rejeq.cpcam.core.stream.relay.VideoRelay
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class CameraVideoTarget @Inject constructor(
    private val target: RecordCameraTarget,
    @ApplicationScope scope: CoroutineScope,
) : VideoTarget {
    private var relay: VideoRelay? = null

    init {
        target.surfaceRequest.onEach {
            when (it) {
                is SurfaceRequestState.Available -> {
                    relay?.let { encoder ->
                        attachRelay(it.value, encoder)
                    }
                }
                else -> {}
            }
        }.launchIn(scope)
    }

    override fun setRelay(relay: VideoRelay) {
        Log.i(TAG, "Updating relay to: $relay")

        when (val request = target.surfaceRequest.value) {
            is SurfaceRequestState.Available -> {
                attachRelay(request.value, relay)
            }
            else -> {}
        }

        this.relay = relay
    }

    override fun start() {
        target.start()
        relay?.start()
    }

    override fun stop() {
        target.stop()
        relay?.stop()
    }

    private fun attachRelay(
        surfaceRequest: SurfaceRequestWrapper,
        relay: VideoRelay,
    ) {
        Log.d(TAG, "Attaching relay to surface request")

        val res = surfaceRequest.resolution
        relay.setResolution(res.width, res.height)

        surfaceRequest.provideSurface(relay.surface, Runnable::run) { result ->
            Log.i(TAG, "provideSurface result: $result")
        }
    }
}

private const val TAG = "CameraVideoTarget"
