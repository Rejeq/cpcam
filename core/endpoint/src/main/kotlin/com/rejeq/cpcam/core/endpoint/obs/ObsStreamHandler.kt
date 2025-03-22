package com.rejeq.cpcam.core.endpoint.obs

import android.util.Log
import com.rejeq.cpcam.core.data.model.ObsStreamData
import com.rejeq.cpcam.core.data.model.PixFmt
import com.rejeq.cpcam.core.data.model.Resolution
import com.rejeq.cpcam.core.data.model.StreamProtocol
import com.rejeq.cpcam.core.data.model.VideoCodec
import com.rejeq.cpcam.core.data.model.VideoConfig
import com.rejeq.cpcam.core.data.repository.StreamRepository
import com.rejeq.cpcam.core.stream.StreamHandler
import com.rejeq.cpcam.core.stream.StreamResult
import com.rejeq.cpcam.core.stream.VideoStreamConfig
import com.rejeq.cpcam.core.stream.target.CameraVideoTarget
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ObsStreamHandler @AssistedInject constructor(
    streamRepo: StreamRepository,
    videoTarget: CameraVideoTarget,
    // FIXME: Scope leak, cancel when object to be destroyed
    @Assisted scope: CoroutineScope,
) {
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
        VideoStreamConfig(
            target = videoTarget,
            streamData.videoConfig,
        ),
    )

    val data = streamRepo.obsData.map { StreamHandlerState.Valid(it) }
        .stateIn(
            scope,
            SharingStarted.Eagerly,
            StreamHandlerState.Unknown(),
        )

    fun start(): StreamResult<Unit> = when (data.value) {
        is StreamHandlerState.Valid -> {
            // TODO: Do not hardcode
            streamHandler.start()
        }
        is StreamHandlerState.Unknown -> {
            Log.w(TAG, "Unable to start stream: Unknown stream data")
            StreamResult.Failed
        }
    }

    fun stop(): StreamResult<Unit> = streamHandler.stop()

    // TODO:
//    private val updateStreamHandler() {
//    }

    @AssistedFactory
    interface Factory {
        fun create(scope: CoroutineScope): ObsStreamHandler
    }
}

sealed interface StreamHandlerState {
    data class Valid(val value: ObsStreamData) : StreamHandlerState

    class Unknown : StreamHandlerState
}

private const val TAG = "ObsStreamHandler"
