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
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

@OptIn(ExperimentalCoroutinesApi::class)
class MainComponent @AssistedInject constructor(
    endpoint: EndpointHandler,
    cameraFactory: CameraComponent.Factory,
    endpointHandler: EndpointHandler,
    screenRepo: ScreenRepository,
    @Assisted componentContext: ComponentContext,
    @Assisted mainContext: CoroutineContext,
    @Assisted("onSettingsClick") val onSettingsClick: () -> Unit,
    @Assisted("onStartEndpoint") val onStartEndpoint: () -> Unit,
    @Assisted("onStopEndpoint") val onStopEndpoint: () -> Unit,
) : ChildComponent,
    ComponentContext by componentContext {
    private val scope = coroutineScope(mainContext + SupervisorJob())

    val nav = MainNavigation(
        componentContext = this,
    )

    val cam = cameraFactory.create(
        scope = scope,
        componentContext = this,
        onShowPermissionDenied = { nav.showPermissionDenied(it) },
    )

    val streamButtonState = MorphButtonState(MorphIconTarget.Stopped)

    // TODO:
//    val showStreamButton = endpointHandler.canBeStarted
    val showStreamButton = flowOf(true)

    val showSwitchCameraButton = cam.state.map {
        it.error != CameraError.PermissionDenied
    }

    val showInfoButton = endpointHandler.state.map {
        it is EndpointState.Started
    }

    val keepScreenAwake = screenRepo.keepScreenAwake
    val dimScreenDelay = screenRepo.dimScreenDelay

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

    @AssistedFactory
    interface Factory {
        fun create(
            componentContext: ComponentContext,
            mainContext: CoroutineContext,
            @Assisted("onSettingsClick") onSettingsClick: () -> Unit,
            @Assisted("onStartEndpoint") onStartEndpoint: () -> Unit,
            @Assisted("onStopEndpoint") onStopEndpoint: () -> Unit,
        ): MainComponent
    }
}
