package com.rejeq.cpcam.core.endpoint

import android.util.Log
import com.rejeq.cpcam.core.data.model.EndpointConfig
import com.rejeq.cpcam.core.data.model.ObsConfig
import com.rejeq.cpcam.core.data.repository.EndpointRepository
import com.rejeq.cpcam.core.endpoint.di.WebsocketClient
import com.rejeq.cpcam.core.endpoint.obs.ObsEndpoint
import com.rejeq.cpcam.core.endpoint.obs.checkObsConnection
import com.rejeq.cpcam.core.endpoint.obs.toEndpointError
import io.ktor.client.HttpClient
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

@Singleton
class EndpointHandler @Inject constructor(
    @WebsocketClient private val wbClientLazy: Lazy<HttpClient>,
    private val endpointRepo: EndpointRepository,
    private val obsEndpointFactory: ObsEndpoint.Factory,
) {
    private val wbClient by wbClientLazy

    private val currEndpoint = MutableStateFlow<Endpoint?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val state = currEndpoint.flatMapLatest {
        it?.state ?: flowOf(DEFAULT_ENDPOINT_STATE)
    }

    suspend fun connect(): EndpointState {
        val endpoint = retrieveLatestEndpoint()
        if (endpoint == null) {
            Log.w(TAG, "Unable to connect: Fails retrieve endpoint")
            return DEFAULT_ENDPOINT_STATE
        }

        return endpoint.connect()
    }

    suspend fun disconnect(): EndpointState {
        val endpoint = currEndpoint.value
        if (endpoint == null) {
            Log.w(TAG, "Unable to disconnect: Not started")
            return DEFAULT_ENDPOINT_STATE
        }

        val newState = endpoint.disconnect()
        return newState
    }

    suspend fun checkConnection(config: EndpointConfig): EndpointErrorKind? {
        val error = when (config) {
            is ObsConfig -> {
                checkObsConnection(wbClient, config)?.toEndpointError()
            }
        }

        return error
    }

    suspend fun retrieveLatestEndpoint(): Endpoint? {
        val newConfig = endpointRepo.endpointConfig.first()
        val currentEndpoint = currEndpoint.value

        return when {
            currentEndpoint == null -> {
                makeEndpoint(newConfig).also {
                    currEndpoint.value = it
                }
            }
            currentEndpoint.config != newConfig -> {
                disconnect()
                makeEndpoint(newConfig).also {
                    currEndpoint.value = it
                }
            }
            else -> currentEndpoint
        }
    }

    private fun makeEndpoint(config: EndpointConfig): Endpoint = when (config) {
        is ObsConfig -> obsEndpointFactory.create(config)
    }
}

private val DEFAULT_ENDPOINT_STATE = EndpointState.Stopped(
    EndpointErrorKind.EndpointNotConfigured,
)

private const val TAG = "EndpointHandler"
