package com.rejeq.cpcam.core.endpoint.obs

import android.util.Log
import com.rejeq.cpcam.core.data.model.ObsConfig
import com.rejeq.cpcam.core.data.model.StreamProtocol
import com.rejeq.cpcam.core.data.repository.StreamRepository
import com.rejeq.cpcam.core.endpoint.Endpoint
import com.rejeq.cpcam.core.endpoint.EndpointState
import com.rejeq.cpcam.core.endpoint.di.WebsocketClient
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.ktor.client.HttpClient
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

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

    override suspend fun connect(): EndpointState = coroutineScope {
        val data = streamRepo.obsData.first()

        val connJob = async {
            connHandler.start(data)
        }

        val streamJob = async {
            streamHandler.start(data)
        }

        getState(connJob.await(), streamJob.await())
    }

    override suspend fun disconnect(): EndpointState {
        val streamState = streamHandler.stop()
        val connState = connHandler.stop()

        return getState(connState, streamState)
    }

    private fun getState(
        conn: ConnectionState,
        stream: StreamHandlerState,
    ): EndpointState {
        Log.d(TAG, "getState: conn=$conn, stream=$stream")

        val newState = if (stream == StreamHandlerState.Started &&
            conn is ConnectionState.Stopped &&
            conn.reason != null
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
