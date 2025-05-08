package com.rejeq.cpcam.feature.main

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.rejeq.cpcam.core.camera.CameraError
import com.rejeq.cpcam.core.camera.CameraType
import com.rejeq.cpcam.core.common.ChildComponent
import com.rejeq.cpcam.core.data.repository.ScreenRepository
import com.rejeq.cpcam.core.endpoint.EndpointHandler
import com.rejeq.cpcam.core.endpoint.EndpointState
import com.rejeq.cpcam.core.ui.MorphButtonState
import com.rejeq.cpcam.core.ui.MorphIconTarget
import com.rejeq.cpcam.feature.main.camera.CameraComponent
import com.rejeq.cpcam.feature.main.camera.DefaultCameraComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.StateFlow

interface MainComponent : ChildComponent {
    val nav: MainNavigation
    val cam: CameraComponent
    val streamButtonState: MorphButtonState
    val showStreamButton: StateFlow<Boolean>
    val showSwitchCameraButton: StateFlow<Boolean>
    val showInfoButton: StateFlow<Boolean>
    val keepScreenAwake: StateFlow<Boolean>
    val dimScreenDelay: StateFlow<Long?>

    fun onSettingsClick()
    fun onStartEndpoint()
    fun onStopEndpoint()
}

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultMainComponent @AssistedInject constructor(
    endpoint: EndpointHandler,
    cameraFactory: DefaultCameraComponent.Factory,
    endpointHandler: EndpointHandler,
    screenRepo: ScreenRepository,
    @Assisted componentContext: ComponentContext,
    @Assisted mainContext: CoroutineContext,
    @Assisted("onSettingsClick") val onSettingsClick: () -> Unit,
    @Assisted("onStartEndpoint") val onStartEndpoint: () -> Unit,
    @Assisted("onStopEndpoint") val onStopEndpoint: () -> Unit,
) : MainComponent,
    ComponentContext by componentContext {
    private val scope = coroutineScope(mainContext + SupervisorJob())

    override val nav = DefaultMainNavigation(
        componentContext = this,
    )

    override val cam = cameraFactory.create(
        scope = scope,
        componentContext = this,
        onShowPermissionDenied = { nav.showPermissionDenied(it) },
    )

    override val streamButtonState = MorphButtonState(MorphIconTarget.Stopped)

    // TODO:
//    val showStreamButton = endpointHandler.canBeStarted
    override val showStreamButton = MutableStateFlow(true).asStateFlow()

    override val showSwitchCameraButton = cam.state.map {
        it.error != CameraError.PermissionDenied
    }.stateIn(
        scope,
        SharingStarted.WhileSubscribed(5_000),
        false,
    )

    override val showInfoButton = endpointHandler.state.map {
        it is EndpointState.Started
    }.stateIn(
        scope,
        SharingStarted.WhileSubscribed(5_000),
        false,
    )

    override val keepScreenAwake = screenRepo.keepScreenAwake.stateIn(
        scope,
        SharingStarted.WhileSubscribed(5_000),
        false,
    )

    override val dimScreenDelay = screenRepo.dimScreenDelay.stateIn(
        scope,
        SharingStarted.WhileSubscribed(5_000),
        null,
    )

    init {
        endpoint.state
            .onEach {
                streamButtonState.animTarget = when (it) {
                    is EndpointState.Stopped -> MorphIconTarget.Stopped
                    is EndpointState.Started -> MorphIconTarget.Started
                    is EndpointState.Connecting -> MorphIconTarget.Loading
                }
            }
            .launchIn(scope)
    }

    override fun readyToShow(): Boolean {
        val state = cam.state.value

        return state.type == CameraType.Open || state.error != null
    }

    override fun onSettingsClick() = onSettingsClick.invoke()
    override fun onStartEndpoint() = onStartEndpoint.invoke()
    override fun onStopEndpoint() = onStopEndpoint.invoke()

    @AssistedFactory
    interface Factory {
        fun create(
            componentContext: ComponentContext,
            mainContext: CoroutineContext,
            @Assisted("onSettingsClick") onSettingsClick: () -> Unit,
            @Assisted("onStartEndpoint") onStartEndpoint: () -> Unit,
            @Assisted("onStopEndpoint") onStopEndpoint: () -> Unit,
        ): DefaultMainComponent
    }
}
