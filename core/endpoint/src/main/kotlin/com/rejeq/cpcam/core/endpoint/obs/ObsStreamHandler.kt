package com.rejeq.cpcam.core.endpoint.obs

import com.rejeq.cpcam.core.data.model.ObsStreamData
import com.rejeq.cpcam.core.data.model.PixFmt
import com.rejeq.cpcam.core.data.model.Resolution
import com.rejeq.cpcam.core.data.model.StreamProtocol
import com.rejeq.cpcam.core.data.model.VideoCodec
import com.rejeq.cpcam.core.data.model.VideoConfig
import com.rejeq.cpcam.core.data.model.VideoRelayConfig
import com.rejeq.cpcam.core.data.repository.StreamRepository
import com.rejeq.cpcam.core.endpoint.EndpointResult
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
import kotlinx.coroutines.flow.asStateFlow

class ObsStreamHandler @AssistedInject constructor(
    streamRepo: StreamRepository,
    videoTarget: CameraVideoTarget,
    // FIXME: Scope leak, cancel when object to be destroyed
    @Assisted scope: CoroutineScope,
) {
    private val _state =
        MutableStateFlow<StreamHandlerState>(StreamHandlerState.Stopped)
    val state = _state.asStateFlow()

    // TODO: Do not hardcode. Update from settings
    val streamData = ObsStreamData(
        StreamProtocol.MPEGTS,
        // TODO: Change to tcp
//        "tcp://192.168.1.101:10002/?listen",
        "udp://192.168.1.101:10002",
        VideoConfig(
            codecName = VideoCodec.H264,
            pixFmt = PixFmt.NV12,
//            pixFmt = PixFmt.YUV420P,
            bitrate = 8_000_000,
            framerate = 30,
            resolution = Resolution(1280, 720),
        ),
    )

    val streamHandler = StreamHandler(
        streamData.protocol,
        streamData.host,
        videoStreamConfig = VideoStreamConfig(
            target = videoTarget,
            data = streamData.videoConfig,
        ),
    ).also {
        it.setVideoRelayConfig(
            VideoRelayConfig(
                resolution = streamData.videoConfig.resolution,
                framerate = null,
            ),
        )
    }

    fun start(): StreamResult<Unit> {
        _state.value = StreamHandlerState.Connecting
        val res = streamHandler.start()
        _state.value = StreamHandlerState.Started

        return res
    }

    fun stop(): StreamResult<Unit> {
        val res = streamHandler.stop()
        _state.value = StreamHandlerState.Stopped

        return res
    }

    // TODO:
//    private val updateStreamHandler() {
//    }

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
    StreamHandlerState.Stopped -> EndpointState.Stopped
    StreamHandlerState.Connecting -> EndpointState.Connecting
    StreamHandlerState.Started -> EndpointState.Started(EndpointResult.Success)
}

private const val TAG = "ObsStreamHandler"
