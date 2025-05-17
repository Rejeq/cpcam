package com.rejeq.cpcam.core.endpoint.obs

import android.util.Log
import com.github.michaelbull.result.mapBoth
import com.rejeq.cpcam.core.data.model.ObsConfig
import com.rejeq.cpcam.core.data.model.ObsStreamData
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface ConnectionState {
    data object Started : ConnectionState
    data object Connecting : ConnectionState
    data class Stopped(val reason: ObsErrorKind?) : ConnectionState
}

class ObsConnectionHandler(
    private val config: ObsConfig,
    private val wbClient: HttpClient,
) {
    private val _state =
        MutableStateFlow<ConnectionState>(ConnectionState.Stopped(null))
    val state = _state.asStateFlow()

    suspend fun start(streamData: ObsStreamData?): ConnectionState {
        if (streamData == null) {
            Log.w(TAG, "Does not have stream data")

            _state.value = ConnectionState.Stopped(ObsErrorKind.NotHaveData)
            return _state.value
        }

        _state.value = ConnectionState.Connecting

        val result = obsConnect(wbClient, config) {
            Log.i(TAG, "Successfully connected to the endpoint")

            setupObsScene(streamData)
        }

        _state.value = result.mapBoth(
            { ConnectionState.Started },
            { err -> ConnectionState.Stopped(err) },
        )

        return _state.value
    }

    fun stop(): ConnectionState {
        _state.value = ConnectionState.Stopped(null)
        // TODO: Maybe make obs input invisible?

        return _state.value
    }
}

private const val TAG = "ObsConnectionHandler"
