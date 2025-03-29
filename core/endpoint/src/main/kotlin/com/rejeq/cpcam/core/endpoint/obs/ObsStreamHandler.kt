package com.rejeq.cpcam.core.endpoint.obs

import android.util.Log
import com.rejeq.cpcam.core.data.model.ObsStreamData
import com.rejeq.cpcam.core.data.model.VideoConfig
import com.rejeq.cpcam.core.data.model.VideoRelayConfig
import com.rejeq.cpcam.core.data.repository.StreamRepository
import com.rejeq.cpcam.core.endpoint.EndpointState
import com.rejeq.cpcam.core.stream.StreamHandler
import com.rejeq.cpcam.core.stream.StreamResult
import com.rejeq.cpcam.core.stream.VideoStreamConfig
import com.rejeq.cpcam.core.stream.target.CameraVideoTarget
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

class ObsStreamHandler @AssistedInject constructor(
    streamRepo: StreamRepository,
    private val videoTarget: CameraVideoTarget,
    // FIXME: Scope leak, cancel when object to be destroyed
    @Assisted scope: CoroutineScope,
) {
    private val _state =
        MutableStateFlow<StreamHandlerState>(StreamHandlerState.Stopped)
    val state = _state.asStateFlow()

    private var streamHandler: StreamHandler? = null

    val streamData = streamRepo.obsData
        .onEach(action = ::updateStreamHandler)
        .stateIn(
            scope,
            SharingStarted.WhileSubscribed(5_000),
            null,
        )

    fun start(): StreamHandlerState {
        _state.value = StreamHandlerState.Connecting
        val res = streamHandler?.start() ?: StreamResult.Failed

        _state.value = when (res) {
            StreamResult.Failed -> StreamHandlerState.Stopped
            is StreamResult.Success -> StreamHandlerState.Started
        }

        return _state.value
    }

    fun stop(): StreamHandlerState {
        streamHandler?.stop()
        _state.value = StreamHandlerState.Stopped

        return _state.value
    }

    private fun updateStreamHandler(data: ObsStreamData) {
        Log.i(TAG, "Updating stream handler to: $data")

        val oldHandler = streamHandler
        if (oldHandler != null &&
            oldHandler.protocol == data.protocol &&
            oldHandler.host == data.host
        ) {
            oldHandler.updateStreamVideoRelay(data.videoConfig)
            return
        }

        streamHandler = StreamHandler(
            data.protocol,
            data.host,
            videoStreamConfig = VideoStreamConfig(
                target = videoTarget,
                data = data.videoConfig,
            ),
        ).also {
            it.updateStreamVideoRelay(data.videoConfig)
        }
    }

    private fun StreamHandler.updateStreamVideoRelay(data: VideoConfig) {
        this.setVideoRelayConfig(
            VideoRelayConfig(
                resolution = data.resolution,
                framerate = null,
            ),
        )
    }

    @AssistedFactory
    interface Factory {
        fun create(scope: CoroutineScope): ObsStreamHandler
    }
}

enum class StreamHandlerState {
    Stopped,
    Connecting,
    Started,
}

fun StreamHandlerState.toEndpointState() = when (this) {
    StreamHandlerState.Stopped -> EndpointState.Stopped(null)
    StreamHandlerState.Connecting -> EndpointState.Connecting
    StreamHandlerState.Started -> EndpointState.Started(null)
}

private const val TAG = "ObsStreamHandler"
