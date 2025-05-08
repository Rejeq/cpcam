package com.rejeq.cpcam.feature.settings.endpoint

import android.util.Log
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.rejeq.cpcam.core.common.ChildComponent
import com.rejeq.cpcam.core.common.di.ApplicationScope
import com.rejeq.cpcam.core.data.repository.EndpointRepository
import com.rejeq.cpcam.core.data.repository.StreamRepository
import com.rejeq.cpcam.core.endpoint.EndpointHandler
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

interface EndpointComponent : ChildComponent {
    val endpointConfig: StateFlow<FormState<EndpointConfigForm>>
    val streamData: StateFlow<FormState<ObsStreamDataForm>>
    val connectionState: StateFlow<EndpointConnectionState>

    fun onEndpointChange(data: EndpointConfigForm)
    fun onStreamDataChange(data: ObsStreamDataForm)
    fun onCheckConnection()
    fun onFinished()
}

@OptIn(FlowPreview::class)
class DefaultEndpointComponent @AssistedInject constructor(
    val endpointRepo: EndpointRepository,
    val streamRepo: StreamRepository,
    val endpointHandler: EndpointHandler,
    @ApplicationScope private val externalScope: CoroutineScope,

    @Assisted componentContext: ComponentContext,
    @Assisted mainContext: CoroutineContext,
    @Assisted("onFinished") val onFinished: () -> Unit,
) : EndpointComponent,
    ComponentContext by componentContext {
    private val scope = coroutineScope(mainContext + SupervisorJob())

    private val _endpointConfig =
        MutableStateFlow<FormState<EndpointConfigForm>>(FormState.Loading)
    override val endpointConfig = _endpointConfig.asStateFlow()

    private val _streamData =
        MutableStateFlow<FormState<ObsStreamDataForm>>(FormState.Loading)
    override val streamData = _streamData.asStateFlow()

    private var checkEndpointJob: Job? = null
    private val _connectionState = MutableStateFlow<EndpointConnectionState>(
        EndpointConnectionState.NotStarted,
    )
    override val connectionState = _connectionState.asStateFlow()

    init {
        scope.launch {
            val config = endpointRepo.endpointConfig.first()
            _endpointConfig.value = FormState.Success(config.fromDomain())
        }

        scope.launch {
            val obsData = streamRepo.obsData.first()
            _streamData.value = FormState.Success(obsData.fromDomain())
        }
    }

    override fun onEndpointChange(data: EndpointConfigForm) {
        _endpointConfig.value = FormState.Success(data)

        externalScope.launch {
            when (data) {
                is ObsConfigForm -> endpointRepo.setObsConfig(data.toDomain())
            }
        }
    }

    override fun onStreamDataChange(data: ObsStreamDataForm) {
        _streamData.value = FormState.Success(data)

        externalScope.launch {
            streamRepo.setObsData(data.toDomain())
        }
    }

    override fun onCheckConnection() {
        _connectionState.value = EndpointConnectionState.Connecting
        checkEndpointJob?.cancel()

        when (val state = endpointConfig.value) {
            is FormState.Success -> {
                checkEndpointJob = scope.launch {
                    val error = endpointHandler.checkConnection(
                        state.data.toDomain(),
                    )

                    _connectionState.value = when (error) {
                        null -> EndpointConnectionState.Success
                        else -> EndpointConnectionState.Failed
                    }
                }
            }
            is FormState.Loading -> {
                Log.i(TAG, "Unable check connection: Endpoint not configured")
                _connectionState.value = EndpointConnectionState.Failed
            }
        }
    }

    override fun onFinished() = onFinished.invoke()

    @AssistedFactory
    interface Factory {
        fun create(
            componentContext: ComponentContext,
            mainContext: CoroutineContext,
            @Assisted("onFinished") onFinished: () -> Unit,
        ): DefaultEndpointComponent
    }
}

enum class EndpointConnectionState {
    NotStarted,
    Failed,
    Connecting,
    Success,
}

private const val TAG = "EndpointComponent"
