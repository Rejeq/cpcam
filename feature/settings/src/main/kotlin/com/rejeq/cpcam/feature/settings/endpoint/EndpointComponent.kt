package com.rejeq.cpcam.feature.settings.endpoint

import android.util.Log
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.rejeq.cpcam.core.common.ChildComponent
import com.rejeq.cpcam.core.common.di.ApplicationScope
import com.rejeq.cpcam.core.data.model.EndpointConfig
import com.rejeq.cpcam.core.data.model.ObsConfig
import com.rejeq.cpcam.core.data.repository.EndpointRepository
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
    val endpointHandler: EndpointHandler,
    @ApplicationScope private val externalScope: CoroutineScope,

    @Assisted componentContext: ComponentContext,
    @Assisted mainContext: CoroutineContext,
    @Assisted val onFinished: () -> Unit,
) : ChildComponent,
    ComponentContext by componentContext {
    private val scope = coroutineScope(mainContext + SupervisorJob())

    private val _endpointForm =
        MutableStateFlow<EndpointFormState>(EndpointFormState.Loading)
    val endpointForm = _endpointForm.asStateFlow()

    private var checkEndpointJob: Job? = null
    private val _connectionState = MutableStateFlow<EndpointConnectionState>(
        EndpointConnectionState.NotStarted,
    )
    val connectionState = _connectionState.asStateFlow()

    init {
        endpointRepo.endpointConfig.onEach {
            _endpointForm.value = EndpointFormState.Success(it)
        }.launchIn(scope)
    }

    fun updateEndpoint(data: EndpointConfig) {
        _endpointForm.value = EndpointFormState.Success(data)

        externalScope.launch {
//            val port = state.port
//            if (port == null) {
//                _connectionState.value = EndpointConnectionState.Failed
//                return@launch
//            }

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

    fun checkEndpointConnection() {
        _connectionState.value = EndpointConnectionState.Connecting
        checkEndpointJob?.cancel()

        when (val state = endpointForm.value) {
            is EndpointFormState.Success -> {
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
            is EndpointFormState.Loading -> {
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

sealed interface EndpointFormState {
    object Loading : EndpointFormState

    data class Success(val data: EndpointConfig) : EndpointFormState
}

enum class EndpointConnectionState {
    NotStarted,
    Failed,
    Connecting,
    Success,
}

private const val TAG = "EndpointComponent"
