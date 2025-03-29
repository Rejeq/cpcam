package com.rejeq.cpcam.core.endpoint.obs

import android.util.Log
import com.rejeq.cpcam.core.data.model.ObsConfig
import com.rejeq.cpcam.core.endpoint.Endpoint
import com.rejeq.cpcam.core.endpoint.EndpointState
import io.ktor.client.HttpClient
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

class ObsEndpoint(
    val config: ObsConfig,
    private val streamHandler: ObsStreamHandler,
    wbClient: HttpClient,
) : Endpoint {
    private val connHandler = ObsConnectionHandler(config, wbClient)

    override val state =
        combine(connHandler.state, streamHandler.state) { conn, stream ->
            if (stream == StreamHandlerState.Started &&
                conn is ConnectionState.Stopped &&
                conn.reason != null
            ) {
                EndpointState.Started(conn.reason.toEndpointResult())
            } else {
                stream.toEndpointState()
            }
        }

    override suspend fun connect(): Unit = coroutineScope {
        val connJob = launch {
            val streamData = streamHandler.streamData.value
            if (streamData == null) {
                Log.w(TAG, "Does not have stream data")
                return@launch
            }

            connHandler.start(streamData)
        }

        val streamJob = launch {
            streamHandler.start()
        }

        joinAll(connJob, streamJob)
    }

    override suspend fun disconnect() {
        streamHandler.stop()
        connHandler.stop()
    }
}

private const val TAG = "ObsEndpoint"
