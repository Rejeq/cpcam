package com.rejeq.cpcam.core.endpoint.obs

import android.util.Log
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.mapBoth
import com.github.michaelbull.result.mapError
import com.rejeq.cpcam.core.common.di.ApplicationScope
import com.rejeq.cpcam.core.data.model.ObsStreamData
import com.rejeq.cpcam.core.endpoint.EndpointState
import com.rejeq.cpcam.core.stream.SessionConfig
import com.rejeq.cpcam.core.stream.SessionHolder
import com.rejeq.cpcam.core.stream.SessionRunner
import com.rejeq.cpcam.core.stream.VideoStreamConfig
import com.rejeq.cpcam.core.stream.target.CameraVideoTarget
import com.rejeq.cpcam.core.stream.target.VideoTarget
import javax.inject.Inject
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ObsStreamHandler @Inject constructor(
    @ApplicationScope private val appScope: CoroutineScope,
    private val videoTarget: CameraVideoTarget,
) {
    private val sessionHolder = SessionHolder()
    private var streamJob: Job? = null

    private val _state =
        MutableStateFlow<StreamHandlerState>(StreamHandlerState.Stopped)
    val state = _state.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun start(
        config: ObsStreamData?,
    ): Result<Unit, ObsStreamErrorKind> {
        _state.value = StreamHandlerState.Connecting

        val runner = getConfiguredRunner(config).getOrElse { err ->
            Log.w(TAG, "Unable to start stream handler: $err")

            _state.value = StreamHandlerState.Failed(err)
            return Err(err)
        }

        val startResult =
            CompletableDeferred<Result<Unit, ObsStreamErrorKind>>()

        streamJob?.cancel()
        streamJob = appScope.launch {
            var runnerResult: Result<Unit, ObsStreamErrorKind>? = null

            try {
                runnerResult = runner.use {
                    _state.value = StreamHandlerState.Started
                    startResult.complete(Ok(Unit))

                    awaitCancellation()
                }.mapError { it.toObsStreamError() }

                startResult.complete(runnerResult)
            } finally {
                _state.value = runnerResult?.mapBoth(
                    success = { StreamHandlerState.Stopped },
                    failure = { StreamHandlerState.Failed(it) },
                ) ?: StreamHandlerState.Stopped
            }
        }

        return startResult.await()
    }

    fun stop() {
        streamJob?.cancel()
    }

    private fun getConfiguredRunner(
        data: ObsStreamData?,
    ): Result<SessionRunner, ObsStreamErrorKind> {
        if (data == null) {
            return Err(ObsStreamErrorKind.NoStreamData)
        }

        val runner = sessionHolder.getConfigured(
            data.toStreamConfig(videoTarget),
        )

        return runner.mapError { it.toObsStreamError() }
    }
}

fun ObsStreamData.toStreamConfig(target: VideoTarget): SessionConfig =
    SessionConfig(
        protocol = this.protocol,
        host = this.host,
        videoStreamConfig = VideoStreamConfig(
            target = target,
            data = this.videoConfig,
        ),
    )

sealed interface StreamHandlerState {
    data object Stopped : StreamHandlerState
    data object Connecting : StreamHandlerState
    data object Started : StreamHandlerState
    data class Failed(val reason: ObsStreamErrorKind) : StreamHandlerState
}

fun StreamHandlerState.toEndpointState() = when (this) {
    is StreamHandlerState.Stopped -> EndpointState.Stopped
    is StreamHandlerState.Connecting -> EndpointState.Connecting
    is StreamHandlerState.Started -> EndpointState.Started(null)
    is StreamHandlerState.Failed -> EndpointState.Failed(
        this.reason.toEndpointError(),
    )
}

private const val TAG = "ObsStreamHandler"
