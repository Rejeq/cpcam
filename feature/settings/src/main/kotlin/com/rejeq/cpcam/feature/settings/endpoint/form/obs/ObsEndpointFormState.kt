package com.rejeq.cpcam.feature.settings.endpoint.form.obs

import com.github.michaelbull.result.mapBoth
import com.rejeq.cpcam.core.data.model.EndpointType
import com.rejeq.cpcam.core.data.model.ObsStreamData
import com.rejeq.cpcam.core.data.model.PixFmt
import com.rejeq.cpcam.core.data.model.StreamProtocol
import com.rejeq.cpcam.core.data.model.VideoCodec
import com.rejeq.cpcam.core.data.repository.EndpointRepository
import com.rejeq.cpcam.core.data.repository.StreamRepository
import com.rejeq.cpcam.core.endpoint.EndpointHandler
import com.rejeq.cpcam.feature.settings.endpoint.ObsConnectionState
import com.rejeq.cpcam.feature.settings.endpoint.form.DEFAULT_FORM_DEBOUNCE_DELAY
import com.rejeq.cpcam.feature.settings.endpoint.form.EndpointFormState
import com.rejeq.cpcam.feature.settings.endpoint.form.FormState
import com.rejeq.cpcam.feature.settings.endpoint.form.stream.StreamFormData
import com.rejeq.cpcam.feature.settings.endpoint.form.stream.StreamFormState
import com.rejeq.cpcam.feature.settings.endpoint.form.video.fromDomain
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

interface ObsEndpointFormState : EndpointFormState {
    val configState: StateFlow<FormState<ObsConfigFormState>>
    val streamState: StateFlow<FormState<StreamFormState>>
    val connState: StateFlow<ObsConnectionState>

    fun onCheckConnection(formState: ObsConfigFormState)
}

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class DefaultObsEndpointFormState @AssistedInject constructor(
    val endpointRepo: EndpointRepository,
    val streamRepo: StreamRepository,
    val endpointHandler: EndpointHandler,

    @Assisted("scope") val scope: CoroutineScope,
) : ObsEndpointFormState {
    private val _configState =
        MutableStateFlow<FormState<ObsConfigFormState>>(FormState.Loading)
    override val configState = _configState.asStateFlow()

    private val _connState = MutableStateFlow(ObsConnectionState.NotStarted)
    override val connState = _connState.asStateFlow()

    private val _streamState =
        MutableStateFlow<FormState<StreamFormState>>(FormState.Loading)
    override val streamState = _streamState.asStateFlow()

    init {
        scope.launch {
            val config = endpointRepo.obsConfig.first()
            val state = ObsConfigFormState(
                config.url,
                config.password,
                config.port,
            )

            _configState.value = FormState.Success(state)

            configState
                .filter { it is FormState.Success }
                .flatMapLatest { (it as FormState.Success).data.state }
                .debounce(DEFAULT_FORM_DEBOUNCE_DELAY)
                .distinctUntilChanged()
                .collect { endpointRepo.setObsConfig(it) }
        }

        scope.launch {
            val obsData = streamRepo.obsData.first()
            _streamState.value = FormState.Success(makeStreamForm(obsData))

            streamState
                .filter { it is FormState.Success }
                .flatMapLatest { (it as FormState.Success).data.state }
                .debounce(DEFAULT_FORM_DEBOUNCE_DELAY)
                .distinctUntilChanged()
                .collect { streamRepo.setObsData(it.toDomain()) }
        }
    }

    private var checkEndpointJob: Job? = null
    override fun onCheckConnection(formState: ObsConfigFormState) {
        _connState.value = ObsConnectionState.Connecting

        checkEndpointJob?.cancel()
        checkEndpointJob = scope.launch {
            val state = formState.state.first()

            _connState.value = endpointHandler.checkConnection(state).mapBoth(
                success = { ObsConnectionState.Success },
                failure = { ObsConnectionState.Failed },
            )
        }
    }

    override suspend fun saveState() {
        val streamState = (streamState.value as? FormState.Success)
            ?.data?.state?.first()

        if (streamState != null) {
            streamRepo.setObsData(streamState.toDomain())
        }
    }

    fun makeStreamForm(data: ObsStreamData): StreamFormState {
        val protocols = endpointHandler.getSupportedProtocols(
            EndpointType.OBS,
        )

        val codecs = endpointHandler.getSupportedCodecs()

        val formats = data.videoConfig.codecName?.let {
            endpointHandler.getSupportedFormats(it)
        } ?: emptyList()

        return data.fromDomain(
            protocols,
            codecs,
            formats,
            getAvailablePixFmts = { endpointHandler.getSupportedFormats(it) },
        )
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("scope") scope: CoroutineScope,
        ): DefaultObsEndpointFormState
    }
}

fun ObsStreamData.fromDomain(
    protocols: List<StreamProtocol>,
    codecs: List<VideoCodec>,
    formats: List<PixFmt>,
    getAvailablePixFmts: (VideoCodec) -> List<PixFmt>,
) = StreamFormState(
    initProtocol = protocol,
    initAvailableProtocols = protocols,
    initHost = host,
    videoFormState = videoConfig.fromDomain(
        codecs,
        formats,
        getAvailablePixFmts = getAvailablePixFmts,
    ),
)

fun StreamFormData.toDomain(): ObsStreamData = ObsStreamData(
    protocol = protocol,
    host = host,
    videoConfig = videoForm.toDomain(),
)
