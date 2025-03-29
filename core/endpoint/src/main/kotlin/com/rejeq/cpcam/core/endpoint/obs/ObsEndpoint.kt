package com.rejeq.cpcam.core.endpoint.obs

import com.rejeq.cpcam.core.data.model.ObsConfig
import com.rejeq.cpcam.core.endpoint.Endpoint
import com.rejeq.cpcam.core.endpoint.EndpointState
import io.ktor.client.HttpClient
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.combine

class ObsEndpoint(
    val config: ObsConfig,
    private val streamHandler: ObsStreamHandler,
    wbClient: HttpClient,
) : Endpoint {
    private val connHandler = ObsConnectionHandler(config, wbClient)

    override val state = combine(
        connHandler.state,
        streamHandler.state,
        transform = ::getState,
    )

    override suspend fun connect(): EndpointState = coroutineScope {
        val connJob = async {
            connHandler.start(streamHandler.streamData.value)
        }

        val streamJob = async {
            streamHandler.start()
        }

        val connState = connJob.await()
        val streamState = streamJob.await()

        getState(connState, streamState)
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
}

private const val TAG = "ObsEndpoint"
