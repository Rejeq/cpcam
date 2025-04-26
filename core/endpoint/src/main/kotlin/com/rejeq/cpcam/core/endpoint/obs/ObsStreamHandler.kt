package com.rejeq.cpcam.core.endpoint.obs

import android.util.Log
import com.rejeq.cpcam.core.data.model.ObsStreamData
import com.rejeq.cpcam.core.endpoint.EndpointState
import com.rejeq.cpcam.core.stream.StreamConfig
import com.rejeq.cpcam.core.stream.StreamHandler
import com.rejeq.cpcam.core.stream.StreamHolder
import com.rejeq.cpcam.core.stream.VideoStreamConfig
import com.rejeq.cpcam.core.stream.target.CameraVideoTarget
import com.rejeq.cpcam.core.stream.target.VideoTarget
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ObsStreamHandler @Inject constructor(
    private val videoTarget: CameraVideoTarget,
) {
    private val streamHolder = StreamHolder()

    private val _state =
        MutableStateFlow<StreamHandlerState>(StreamHandlerState.Stopped())
    val state = _state.asStateFlow()

    fun start(streamData: ObsStreamData?): StreamHandlerState {
        _state.value = StreamHandlerState.Connecting

        val handler = getConfiguredStreamHandler(streamData)
        if (handler == null) {
            Log.w(TAG, "Unable to start stream handler: No stream data")

            _state.value = StreamHandlerState.Stopped(
                ObsStreamErrorKind.NoStreamData,
            )
            return _state.value
        }

        val res = handler.start()
        _state.value = when (res) {
            null -> StreamHandlerState.Started
            else -> StreamHandlerState.Stopped(res.toObsStreamError())
        }

        return _state.value
    }

    fun stop(): StreamHandlerState {
        streamHolder.current?.stop()
        _state.value = StreamHandlerState.Stopped()

        return _state.value
    }

    private fun getConfiguredStreamHandler(
        data: ObsStreamData?,
    ): StreamHandler? {
        if (data == null) {
            return null
        }

        val handler = streamHolder.getConfigured(
            data.toStreamConfig(videoTarget),
        )

        return handler
    }
}

fun ObsStreamData.toStreamConfig(target: VideoTarget): StreamConfig =
    StreamConfig(
        protocol = this.protocol,
        host = this.host,
        videoStreamConfig = VideoStreamConfig(
            target = target,
            data = this.videoConfig,
        ),
    )

sealed interface StreamHandlerState {
    data class Stopped(val reason: ObsStreamErrorKind? = null) :
        StreamHandlerState
    data object Connecting : StreamHandlerState
    data object Started : StreamHandlerState
}

fun StreamHandlerState.toEndpointState() = when (this) {
    is StreamHandlerState.Stopped -> EndpointState.Stopped(
        this.reason?.toEndpointError(),
    )
    is StreamHandlerState.Connecting -> EndpointState.Connecting
    is StreamHandlerState.Started -> EndpointState.Started(null)
}

private const val TAG = "ObsStreamHandler"
