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
import com.rejeq.cpcam.core.endpoint.obs.toEndpointError
import com.rejeq.cpcam.core.endpoint.service.EndpointService
import com.rejeq.cpcam.core.endpoint.service.startEndpointService
import com.rejeq.cpcam.core.endpoint.service.stopEndpointService
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.HttpClient
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

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
    val state = endpoint.flatMapLatest {
        it?.state ?: flowOf(DEFAULT_ENDPOINT_STATE)
    }.stateIn(scope, SharingStarted.Eagerly, DEFAULT_ENDPOINT_STATE)

    init {
        endpointRepo.endpointConfig
            .onEach(action = ::setConfig)
            .launchIn(scope)
    }

    suspend fun connect(): EndpointState {
        val endpoint = _endpoint.value
        if (endpoint == null) {
            Log.w(TAG, "Unable to connect: No endpoint configured")
            return DEFAULT_ENDPOINT_STATE
        }

        startEndpointService(context)
        val newState = endpoint.connect()

        if (newState !is EndpointState.Started) {
            stopEndpointService(context)
        }

        return newState
    }

    suspend fun disconnect(): EndpointState {
        val newState = endpoint.value?.disconnect()

        if (newState is EndpointState.Stopped &&
            EndpointService.isStarted != 0
        ) {
            stopEndpointService(context)
        }

        return newState ?: DEFAULT_ENDPOINT_STATE
    }

    suspend fun checkConnection(config: EndpointConfig): EndpointResult {
        val error = when (config) {
            is ObsConfig -> {
                checkObsConnection(wbClient, config)?.toEndpointError()
            }
        }

        return if (error != null) {
            EndpointResult.Error(error)
        } else {
            EndpointResult.Success
        }
    }

    private suspend fun setConfig(config: EndpointConfig) {
        if (state.value is EndpointState.Stopped) {
            // TODO:
            // hasPendingConfig = true
            endpoint.value?.disconnect()
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

private val DEFAULT_ENDPOINT_STATE = EndpointState.Stopped(
    EndpointErrorKind.EndpointNotConfigured,
)

private const val TAG = "EndpointHandler"
