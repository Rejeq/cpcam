package com.rejeq.cpcam.core.stream.target

import android.util.Log
import com.rejeq.cpcam.core.camera.SurfaceRequestWrapper
import com.rejeq.cpcam.core.camera.target.CameraRequestFlow
import com.rejeq.cpcam.core.camera.target.CameraRequestState
import com.rejeq.cpcam.core.camera.target.RecordCameraTarget
import com.rejeq.cpcam.core.stream.relay.VideoRelay
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class CameraVideoTarget @Inject constructor(
    private val target: RecordCameraTarget,
) : VideoTarget {
    override suspend fun use(
        state: VideoTargetState,
        block: suspend () -> Unit,
    ): Unit = coroutineScope {
        try {
            val request = target.start(state.framerate)
            launchCameraListener(request, state.relay, this@coroutineScope)

            state.relay.start()
            block()
        } finally {
            state.relay.stop()
            target.stop()
        }
    }

    private fun launchCameraListener(
        request: CameraRequestFlow<SurfaceRequestWrapper>,
        relay: VideoRelay,
        scope: CoroutineScope,
    ) {
        request.onEach {
            if (it is CameraRequestState.Available<SurfaceRequestWrapper>) {
                attachRelay(it.value, relay)
            }
        }.launchIn(scope)
    }

    private fun attachRelay(
        surfaceRequest: SurfaceRequestWrapper,
        relay: VideoRelay,
    ) {
        Log.d(TAG, "Attaching relay to surface request")

        surfaceRequest.provideSurface(relay.surface, Runnable::run) { result ->
            Log.i(TAG, "provideSurface result: $result")
        }
    }
}

private const val TAG = "CameraVideoTarget"
