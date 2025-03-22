package com.rejeq.cpcam.core.endpoint.obs

import android.util.Log
import com.rejeq.cpcam.core.data.model.ObsConfig
import com.rejeq.cpcam.core.endpoint.Endpoint
import com.rejeq.cpcam.core.endpoint.EndpointState
import com.rejeq.cpcam.core.stream.StreamResult
import io.ktor.client.HttpClient
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

class ObsEndpoint(
    val config: ObsConfig,
    private val streamHandler: ObsStreamHandler,
    private val wbClient: HttpClient,
) : Endpoint {
    private val _state = MutableStateFlow<EndpointState>(EndpointState.Stopped)
    override val state = _state.asStateFlow()

    override suspend fun connect(): Unit = coroutineScope {
        if (_state.value != EndpointState.Stopped) {
            Log.w(TAG, "Unable to connect: Not stopped")
            return@coroutineScope
        }

        _state.value = EndpointState.Connecting

        val connJob = launch {
            obsConnect(wbClient, config.url, config.port, config.password) {
                Log.i(TAG, "Successfully connected to the endpoint")

                val streamData = streamHandler.streamData
//                if (streamData !is StreamHandlerState.Valid) {
//                    Log.e(TAG, "Unable to get valid stream data")
//
//                    _state.value = EndpointState.Stopped
//                    return@obsConnect
//                }

                setupObsScene(streamData)
            }
        }

        val streamJob = launch {
            when (val res = streamHandler.start()) {
                is StreamResult.Success<*> ->
                    _state.value = EndpointState.Started
                is StreamResult.Failed -> {
                    Log.e(TAG, "Unable to start stream: $res")
                    _state.value = EndpointState.Stopped
                }
            }
        }

        joinAll(connJob, streamJob)
    }

    override suspend fun disconnect() {
        if (_state.value == EndpointState.Stopped) {
            Log.w(TAG, "Unable to stop: Already stopped")
            return
        }

        when (val res = streamHandler.stop()) {
            is StreamResult.Success<*> ->
                _state.value = EndpointState.Stopped
            is StreamResult.Failed -> {
                Log.w(TAG, "Unable to stop stream: $res")
            }
        }

        // TODO: Maybe make obs input invisible?
        return
    }
}

private const val TAG = "ObsEndpoint"
