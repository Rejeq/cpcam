package com.rejeq.cpcam.feature.settings.endpoint

import android.util.Log
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.rejeq.cpcam.core.common.ChildComponent
import com.rejeq.cpcam.core.common.di.ApplicationScope
import com.rejeq.cpcam.core.data.model.EndpointConfig
import com.rejeq.cpcam.core.data.model.ObsConfig
import com.rejeq.cpcam.core.data.model.ObsStreamData
import com.rejeq.cpcam.core.data.model.VideoConfig
import com.rejeq.cpcam.core.data.repository.EndpointRepository
import com.rejeq.cpcam.core.data.repository.StreamRepository
import com.rejeq.cpcam.core.endpoint.EndpointHandler
import com.rejeq.cpcam.core.endpoint.EndpointResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class EndpointComponent @AssistedInject constructor(
    val endpointRepo: EndpointRepository,
    val streamRepo: StreamRepository,
    val endpointHandler: EndpointHandler,
    @ApplicationScope private val externalScope: CoroutineScope,

    @Assisted componentContext: ComponentContext,
    @Assisted mainContext: CoroutineContext,
    @Assisted val onFinished: () -> Unit,
) : ChildComponent,
    ComponentContext by componentContext {
    private val scope = coroutineScope(mainContext + SupervisorJob())

    private val _endpointForm =
        MutableStateFlow<FormState<EndpointConfig>>(FormState.Loading)
    val endpointForm = _endpointForm.asStateFlow()

    private val _videoConfig =
        MutableStateFlow<FormState<VideoConfig>>(FormState.Loading)
    val videoConfig = _videoConfig.asStateFlow()

    private val _streamData =
        MutableStateFlow<FormState<ObsStreamData>>(FormState.Loading)
    val streamData = _streamData.asStateFlow()

    private var checkEndpointJob: Job? = null
    private val _connectionState = MutableStateFlow<EndpointConnectionState>(
        EndpointConnectionState.NotStarted,
    )
    val connectionState = _connectionState.asStateFlow()

    init {
        endpointRepo.endpointConfig.onEach {
            _endpointForm.value = FormState.Success(it)
        }.launchIn(scope)

        streamRepo.obsData.onEach {
            _videoConfig.value = FormState.Success(it.videoConfig)
            _streamData.value = FormState.Success(it)
        }.launchIn(scope)
    }

    fun updateEndpoint(data: EndpointConfig) {
        _endpointForm.value = FormState.Success(data)

        externalScope.launch {
            when (data) {
                is ObsConfig -> endpointRepo.setObsData(
                    ObsConfig(
                        url = data.url,
                        port = data.port,
                        password = data.password,
                    ),
                )
            }
        }
    }

    fun updateStreamData(data: ObsStreamData) {
        _streamData.value = FormState.Success(data)

        externalScope.launch {
            streamRepo.setObsData(data)
        }
    }

    fun updateVideoConfig(data: VideoConfig) {
        _videoConfig.value = FormState.Success(data)

        externalScope.launch {
            streamRepo.setObsVideoConfig(data)
        }
    }

    fun checkEndpointConnection() {
        _connectionState.value = EndpointConnectionState.Connecting
        checkEndpointJob?.cancel()

        when (val state = endpointForm.value) {
            is FormState.Success -> {
                checkEndpointJob = scope.launch {
                    val hasConnection = endpointHandler.checkConnection(
                        state.data,
                    )

                    _connectionState.value = when (hasConnection) {
                        is EndpointResult.Success -> {
                            EndpointConnectionState.Success
                        }
                        else -> {
                            EndpointConnectionState.Failed
                        }
                    }
                }
            }
            is FormState.Loading -> {
                Log.i(TAG, "Unable check connection: Endpoint not configured")
                _connectionState.value = EndpointConnectionState.Failed
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(
            componentContext: ComponentContext,
            mainContext: CoroutineContext,
            onFinished: () -> Unit,
        ): EndpointComponent
    }
}

enum class EndpointConnectionState {
    NotStarted,
    Failed,
    Connecting,
    Success,
}

private const val TAG = "EndpointComponent"
