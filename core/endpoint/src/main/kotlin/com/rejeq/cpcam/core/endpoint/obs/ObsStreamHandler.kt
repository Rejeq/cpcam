package com.rejeq.cpcam.core.endpoint.obs

import android.util.Log
import com.rejeq.cpcam.core.data.model.ObsStreamData
import com.rejeq.cpcam.core.data.model.VideoConfig
import com.rejeq.cpcam.core.data.model.VideoRelayConfig
import com.rejeq.cpcam.core.data.repository.StreamRepository
import com.rejeq.cpcam.core.endpoint.EndpointErrorKind
import com.rejeq.cpcam.core.endpoint.EndpointState
import com.rejeq.cpcam.core.stream.StreamHandler
import com.rejeq.cpcam.core.stream.StreamResult
import com.rejeq.cpcam.core.stream.VideoStreamConfig
import com.rejeq.cpcam.core.stream.target.CameraVideoTarget
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

class ObsStreamHandler @Inject constructor(
    private val streamRepo: StreamRepository,
    private val videoTarget: CameraVideoTarget,
) {
    private val _state =
        MutableStateFlow<StreamHandlerState>(StreamHandlerState.Stopped())
    val state = _state.asStateFlow()

    private var streamHandler: StreamHandler? = null

    suspend fun start(): StreamHandlerState {
        _state.value = StreamHandlerState.Connecting

        val handler = retrieveLatestStreamHandler()
        if (handler == null) {
            Log.w(TAG, "Unable to start stream handler: No stream data")

            _state.value = StreamHandlerState.Stopped(
                StreamErrorKind.NoStreamData,
            )
            return _state.value
        }

        val res = handler.start()
        _state.value = when (res) {
            is StreamResult.Failed -> StreamHandlerState.Stopped(null)
            is StreamResult.Success -> StreamHandlerState.Started
        }

        return _state.value
    }

    fun stop(): StreamHandlerState {
        streamHandler?.stop()
        _state.value = StreamHandlerState.Stopped()

        return _state.value
    }

    private suspend fun retrieveLatestStreamHandler(): StreamHandler? {
        val data = streamRepo.obsData.first()
        val handler = streamHandler

        return when {
            handler == null -> {
                makeStreamHandler(data).also {
                    streamHandler = it
                }
            }
            handler.obsData == data -> {
                stop()
                makeStreamHandler(data).also {
                    streamHandler = it
                }
            }
            else -> streamHandler
        }
    }

    private fun makeStreamHandler(data: ObsStreamData) = StreamHandler(
        data.protocol,
        data.host,
        videoStreamConfig = VideoStreamConfig(
            target = videoTarget,
            data = data.videoConfig,
        ),
    ).also {
        it.updateStreamVideoRelay(data.videoConfig)
    }

    private fun StreamHandler.updateStreamVideoRelay(data: VideoConfig) {
        this.setVideoRelayConfig(
            VideoRelayConfig(
                resolution = data.resolution,
                framerate = data.framerate,
            ),
        )
    }
}

val StreamHandler.obsData: ObsStreamData? get() {
    return ObsStreamData(
        protocol = this.protocol,
        host = this.host,
        videoConfig = this.videoStreamConfig?.data ?: return null,
    )
}

enum class StreamErrorKind {
    NoStreamData,
}

fun StreamErrorKind.toEndpointError() = EndpointErrorKind.StreamError(this)

sealed interface StreamHandlerState {
    data class Stopped(val reason: StreamErrorKind? = null) : StreamHandlerState
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
