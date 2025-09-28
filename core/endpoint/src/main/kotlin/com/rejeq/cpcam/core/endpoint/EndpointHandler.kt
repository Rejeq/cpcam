package com.rejeq.cpcam.core.endpoint

import android.util.Log
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.mapError
import com.rejeq.cpcam.core.data.model.EndpointConfig
import com.rejeq.cpcam.core.data.model.EndpointType
import com.rejeq.cpcam.core.data.model.ObsConfig
import com.rejeq.cpcam.core.data.model.PixFmt
import com.rejeq.cpcam.core.data.model.StreamProtocol
import com.rejeq.cpcam.core.data.model.VideoCodec
import com.rejeq.cpcam.core.data.repository.EndpointRepository
import com.rejeq.cpcam.core.endpoint.di.WebsocketClient
import com.rejeq.cpcam.core.endpoint.obs.ObsEndpoint
import com.rejeq.cpcam.core.endpoint.obs.checkObsConnection
import com.rejeq.cpcam.core.endpoint.obs.toEndpointError
import com.rejeq.cpcam.core.stream.SessionRunner
import io.ktor.client.HttpClient
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

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

    suspend fun connect(): Result<Unit, EndpointErrorKind> {
        val endpoint = retrieveLatestEndpoint()
        if (endpoint == null) {
            Log.w(TAG, "Unable to connect: Fails retrieve endpoint")
            return Err(EndpointErrorKind.FailedRetrieveEndpoint)
        }

        return endpoint.connect()
    }

    suspend fun disconnect() {
        currEndpoint.value?.disconnect()
    }

    suspend fun checkConnection(
        config: EndpointConfig,
    ): Result<Unit, EndpointErrorKind> {
        val error = when (config) {
            is ObsConfig -> {
                checkObsConnection(wbClient, config)
                    .mapError { it.toEndpointError() }
            }
        }

        return error
    }

    fun getSupportedProtocols(type: EndpointType): List<StreamProtocol> =
        when (type) {
            EndpointType.OBS -> ObsEndpoint.supportedProtocols
        }

    fun getSupportedCodecs(): List<VideoCodec> =
        SessionRunner.getSupportedCodecs()

    fun getSupportedFormats(codec: VideoCodec): List<PixFmt> =
        SessionRunner.getSupportedFormats(codec)

    private suspend fun retrieveLatestEndpoint(): Endpoint? {
        val newConfig = endpointRepo.endpointConfig.firstOrNull()
        val currentEndpoint = currEndpoint.value

        return when {
            newConfig == null -> {
                Log.e(TAG, "Unable to retrieve endpoint: No config found")
                null
            }
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

private val DEFAULT_ENDPOINT_STATE = EndpointState.Stopped

private const val TAG = "EndpointHandler"
