package com.rejeq.cpcam.feature.main

import android.util.Log
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.rejeq.cpcam.core.camera.CameraController
import com.rejeq.cpcam.core.camera.CameraError
import com.rejeq.cpcam.core.camera.CameraType
import com.rejeq.cpcam.core.camera.repository.CameraDataRepository
import com.rejeq.cpcam.core.camera.target.PreviewCameraTarget
import com.rejeq.cpcam.core.common.ChildComponent
import com.rejeq.cpcam.core.data.repository.AppearanceRepository
import com.rejeq.cpcam.core.data.repository.ScreenRepository
import com.rejeq.cpcam.core.device.DndListener
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MainComponent @AssistedInject constructor(
    private val endpoint: EndpointHandler,
    appearanceRepo: AppearanceRepository,
    cameraController: CameraController,
    dndListener: DndListener,
    cameraTarget: PreviewCameraTarget,
    endpointHandler: EndpointHandler,
    cameraDataRepo: CameraDataRepository,
    screenRepo: ScreenRepository,
    @Assisted componentContext: ComponentContext,
    @Assisted mainContext: CoroutineContext,
    @Assisted("onSettingsClick") val onSettingsClick: () -> Unit,
    @Assisted("openEndpointSettings") val openEndpointSettings: () -> Unit,
) : ChildComponent,
    ComponentContext by componentContext {
    private val scope = coroutineScope(mainContext + SupervisorJob())

    val nav = MainNavigation(
        componentContext = this,
        openEndpointSettings = openEndpointSettings,
    )

    val cam = CameraComponent(
        dndListener = dndListener,
        scope = scope,
        appearanceRepo = appearanceRepo,
        controller = cameraController,
        target = cameraTarget,
        onShowPermissionDenied = { nav.showPermissionDenied(it) },
        componentContext = this,
        cameraDataRepo = cameraDataRepo,
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

    fun connect() {
        scope.launch {
            Log.i(TAG, "Connecting")

            val newState = endpoint.connect()
            if (newState is EndpointState.Stopped) {
                val reason = newState.reason
                if (reason != null) {
                    nav.showConnectionError(reason)
                } else {
                    Log.w(TAG, "Unable to connect to endpoint: Without reason")
                }
            }
        }
    }

    fun disconnect() {
        scope.launch {
            Log.i(TAG, "Disconnecting")

            endpoint.disconnect()
        }
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
            @Assisted("openEndpointSettings") openEndpointSettings: () -> Unit,
        ): MainComponent
    }
}

private const val TAG = "MainScreenComponent"
