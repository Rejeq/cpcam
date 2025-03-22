package com.rejeq.cpcam.core.endpoint

import android.content.Context
import android.util.Log
import com.rejeq.cpcam.core.common.di.ApplicationScope
import com.rejeq.cpcam.core.data.model.EndpointConfig
import com.rejeq.cpcam.core.data.model.ObsConfig
import com.rejeq.cpcam.core.data.repository.EndpointRepository
import com.rejeq.cpcam.core.endpoint.di.WebsocketClient
import com.rejeq.cpcam.core.endpoint.obs.ObsEndpoint
import com.rejeq.cpcam.core.endpoint.obs.ObsStreamHandler
import com.rejeq.cpcam.core.endpoint.obs.checkObsConnection
import com.rejeq.cpcam.core.endpoint.service.EndpointService
import com.rejeq.cpcam.core.endpoint.service.startEndpointService
import com.rejeq.cpcam.core.endpoint.service.stopEndpointService
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.HttpClient
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Singleton
class EndpointHandler @Inject constructor(
    @ApplicationContext private val context: Context,
    @WebsocketClient private val wbClientLazy: Lazy<HttpClient>,
    @ApplicationScope private val scope: CoroutineScope,
    private val obsStreamHandlerFactory: ObsStreamHandler.Factory,
    endpointRepo: EndpointRepository,
) {
    private val wbClient by wbClientLazy

    private val _endpoint = MutableStateFlow<Endpoint?>(null)
    val endpoint = _endpoint.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: Flow<EndpointState> = endpoint.flatMapLatest {
        it?.state ?: flowOf(EndpointState.Stopped)
    }

    init {
        endpointRepo.endpointConfig
            .onEach(action = ::setConfig)
            .launchIn(scope)
    }

    suspend fun connect() {
        startEndpointService(context)

        val endpoint = _endpoint.value
        if (endpoint == null) {
            Log.w(TAG, "Unable to connect: No endpoint configured")
            return
        }

        if (endpoint.state.value != EndpointState.Started) {
            endpoint.connect()
        } else {
            Log.i(TAG, "Unable to connect: Already connected")
        }
    }

    suspend fun disconnect() {
        endpoint.value?.disconnect()

        if (EndpointService.isStarted != 0) {
            stopEndpointService(context)
        }
    }

    suspend fun checkConnection(config: EndpointConfig) = when (config) {
        is ObsConfig -> checkObsConnection(wbClient, config)
    }

    private suspend fun setConfig(config: EndpointConfig) {
        val endpoint = _endpoint.value
        if (endpoint != null) {
            if (endpoint.state.value != EndpointState.Stopped) {
                endpoint.disconnect()
            }
        }

        _endpoint.value = makeEndpoint(config)
    }

    private fun makeEndpoint(config: EndpointConfig): Endpoint = when (config) {
        is ObsConfig -> ObsEndpoint(
            config = config,
            streamHandler = obsStreamHandlerFactory.create(scope),
            wbClient = wbClient,
        )
    }
}

private const val TAG = "EndpointHandler"
