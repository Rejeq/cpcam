package com.rejeq.cpcam.core.endpoint.obs

import android.util.Log
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.mapError
import com.rejeq.cpcam.core.data.model.ObsConfig
import com.rejeq.cpcam.core.data.model.StreamProtocol
import com.rejeq.cpcam.core.data.repository.StreamRepository
import com.rejeq.cpcam.core.endpoint.Endpoint
import com.rejeq.cpcam.core.endpoint.EndpointErrorKind
import com.rejeq.cpcam.core.endpoint.EndpointState
import com.rejeq.cpcam.core.endpoint.di.WebsocketClient
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.ktor.client.HttpClient
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class ObsEndpoint @AssistedInject constructor(
    private val streamRepo: StreamRepository,
    private val streamHandler: ObsStreamHandler,
    @WebsocketClient wbClientLazy: Lazy<HttpClient>,
    @Assisted override val config: ObsConfig,
) : Endpoint {
    private val connHandler = ObsConnectionHandler(config, wbClientLazy)

    override val state = combine(
        connHandler.state,
        streamHandler.state,
        transform = ::getState,
    )

    override suspend fun connect(): Result<Unit, EndpointErrorKind> =
        coroutineScope {
            val data = streamRepo.obsData.firstOrNull()

            launch {
                connHandler.start(data)
            }

            val result = streamHandler.start(data)
            result.mapError { it.toEndpointError() }
        }

    override suspend fun disconnect() {
        streamHandler.stop()
        connHandler.stop()
    }

    private fun getState(
        conn: ConnectionState,
        stream: StreamHandlerState,
    ): EndpointState {
        Log.d(TAG, "getState: conn=$conn, stream=$stream")

        val newState = if (stream == StreamHandlerState.Started &&
            conn is ConnectionState.Failed
        ) {
            EndpointState.Started(conn.reason.toEndpointError())
        } else {
            stream.toEndpointState()
        }

        return newState
    }

    @AssistedFactory
    interface Factory {
        fun create(config: ObsConfig): ObsEndpoint
    }

    companion object {
        val supportedProtocols = StreamProtocol.entries
    }
}

private const val TAG = "ObsEndpoint"
