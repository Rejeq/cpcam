package com.rejeq.cpcam.feature.settings.endpoint.form.obs

import com.github.michaelbull.result.mapBoth
import com.rejeq.cpcam.core.data.model.EndpointType
import com.rejeq.cpcam.core.data.model.ObsStreamData
import com.rejeq.cpcam.core.data.repository.EndpointRepository
import com.rejeq.cpcam.core.data.repository.StreamRepository
import com.rejeq.cpcam.core.endpoint.EndpointHandler
import com.rejeq.cpcam.feature.settings.endpoint.ObsConnectionState
import com.rejeq.cpcam.feature.settings.endpoint.form.EndpointFormState
import com.rejeq.cpcam.feature.settings.endpoint.form.FormState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

interface ObsEndpointFormState : EndpointFormState {
    val configState: StateFlow<FormState<ObsConfigFormState>>
    val streamState: StateFlow<FormState<ObsStreamFormState>>
    val connState: StateFlow<ObsConnectionState>

    fun onConfigChange(newState: ObsConfigFormState)
    fun onStreamChange(newState: ObsStreamFormState)
    fun onCheckConnection(state: ObsConfigFormState)
}

class DefaultObsEndpointFormState @AssistedInject constructor(
    val endpointRepo: EndpointRepository,
    val streamRepo: StreamRepository,
    val endpointHandler: EndpointHandler,

    @Assisted("scope") val scope: CoroutineScope,
    @Assisted("externalScope") val externalScope: CoroutineScope,
) : ObsEndpointFormState {
    private val _configState =
        MutableStateFlow<FormState<ObsConfigFormState>>(FormState.Loading)
    override val configState = _configState.asStateFlow()

    private val _streamState =
        MutableStateFlow<FormState<ObsStreamFormState>>(FormState.Loading)
    override val streamState = _streamState.asStateFlow()

    private val _connState = MutableStateFlow(ObsConnectionState.NotStarted)
    override val connState = _connState.asStateFlow()

    init {
        scope.launch {
            val config = endpointRepo.obsConfig.first()
            _configState.value = FormState.Success(config.fromDomain())
        }

        scope.launch {
            val obsData = streamRepo.obsData.first()

            _streamState.value = makeStreamState(obsData)
        }
    }

    override fun onConfigChange(newState: ObsConfigFormState) {
        _configState.value = FormState.Success(newState)

        externalScope.launch {
            endpointRepo.setObsConfig(newState.toDomain())
        }
    }

    override fun onStreamChange(newState: ObsStreamFormState) {
        val domainState = newState.toDomain()
        _streamState.value = makeStreamState(domainState)

        externalScope.launch {
            streamRepo.setObsData(domainState)
        }
    }

    private var checkEndpointJob: Job? = null
    override fun onCheckConnection(state: ObsConfigFormState) {
        _connState.value = ObsConnectionState.Connecting

        checkEndpointJob?.cancel()
        checkEndpointJob = scope.launch {
            _connState.value =
                endpointHandler.checkConnection(state.toDomain()).mapBoth(
                    { ObsConnectionState.Success },
                    { ObsConnectionState.Failed },
                )
        }
    }

    fun makeStreamState(data: ObsStreamData): FormState<ObsStreamFormState> {
        val protocols = endpointHandler.getSupportedProtocols(
            EndpointType.OBS,
        )

        val codecs = endpointHandler.getSupportedCodecs()

        val formats = data.videoConfig.codecName?.let {
            endpointHandler.getSupportedFormats(it)
        } ?: emptyList()

        return FormState.Success(data.fromDomain(protocols, codecs, formats))
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("scope") scope: CoroutineScope,
            @Assisted("externalScope") externalScope: CoroutineScope,
        ): DefaultObsEndpointFormState
    }
}
