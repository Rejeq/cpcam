package com.rejeq.cpcam.core.endpoint.obs

import android.util.Log
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.mapBoth
import com.github.michaelbull.result.mapError
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

        val handler = getConfiguredStreamHandler(streamData).getOrElse { err ->
            Log.w(TAG, "Unable to start stream handler: $err")

            _state.value = StreamHandlerState.Stopped(err)
            return _state.value
        }

        _state.value = handler.start().mapBoth(
            success = { StreamHandlerState.Started },
            failure = { StreamHandlerState.Stopped(it.toObsStreamError()) },
        )

        return _state.value
    }

    fun stop(): StreamHandlerState {
        streamHolder.current?.stop()
        _state.value = StreamHandlerState.Stopped()

        return _state.value
    }

    private fun getConfiguredStreamHandler(
        data: ObsStreamData?,
    ): Result<StreamHandler, ObsStreamErrorKind> {
        if (data == null) {
            return Err(ObsStreamErrorKind.NoStreamData)
        }

        val handler = streamHolder.getConfigured(
            data.toStreamConfig(videoTarget),
        )

        return handler.mapError { it.toObsStreamError() }
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
