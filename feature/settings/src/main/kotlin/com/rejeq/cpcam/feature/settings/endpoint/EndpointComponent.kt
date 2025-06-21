package com.rejeq.cpcam.feature.settings.endpoint

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.arkivanov.essenty.lifecycle.doOnStop
import com.rejeq.cpcam.core.common.ChildComponent
import com.rejeq.cpcam.core.common.CodeVerifier
import com.rejeq.cpcam.core.common.QrScannableComponent
import com.rejeq.cpcam.core.common.di.ApplicationScope
import com.rejeq.cpcam.core.data.model.EndpointType
import com.rejeq.cpcam.core.data.repository.EndpointRepository
import com.rejeq.cpcam.feature.settings.endpoint.form.EndpointFormState
import com.rejeq.cpcam.feature.settings.endpoint.form.obs.DefaultObsEndpointFormState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

interface EndpointComponent : ChildComponent {
    val endpointType: StateFlow<EndpointType?>
    val endpointFormState: StateFlow<EndpointFormState?>

    fun onEndpointTypeChange(type: EndpointType)
    fun onFinished()
}

@OptIn(FlowPreview::class)
class DefaultEndpointComponent @AssistedInject constructor(
    val endpointRepo: EndpointRepository,
    val obsState: DefaultObsEndpointFormState.Factory,
    @ApplicationScope private val externalScope: CoroutineScope,

    @Assisted componentContext: ComponentContext,
    @Assisted mainContext: CoroutineContext,
    @Assisted("onFinished") val onFinished: () -> Unit,
    @Assisted("onQrClick") val onQrClick: (CodeVerifier) -> Unit,
) : EndpointComponent,
    QrScannableComponent,
    ComponentContext by componentContext {
    private val scope = coroutineScope(mainContext + SupervisorJob())

    override val endpointType = endpointRepo.type.stateIn(
        scope,
        started = SharingStarted.WhileSubscribed(5_000),
        null,
    )

    override val endpointFormState = endpointType.map {
        when (it) {
            EndpointType.OBS -> obsState.create(scope, onQrClick)
            null -> null
        }
    }.stateIn(
        scope,
        started = SharingStarted.Eagerly,
        null,
    )

    init {
        doOnStop {
            externalScope.launch {
                endpointFormState.value?.saveState()
            }
        }
    }

    override fun onEndpointTypeChange(type: EndpointType) {
        externalScope.launch {
            endpointRepo.setEndpointType(type)
        }
    }

    override fun handleQrCode(value: String) {
        endpointFormState.value?.handleQrCode(value)
    }

    override fun onFinished() = onFinished.invoke()

    @AssistedFactory
    interface Factory {
        fun create(
            componentContext: ComponentContext,
            mainContext: CoroutineContext,
            @Assisted("onFinished") onFinished: () -> Unit,
            @Assisted("onQrClick") onQrClick: (CodeVerifier) -> Unit,
        ): DefaultEndpointComponent
    }
}

enum class ObsConnectionState {
    NotStarted,
    Failed,
    Connecting,
    Success,
}
