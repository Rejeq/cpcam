package com.rejeq.cpcam.feature.main

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.rejeq.cpcam.core.ui.MorphButtonState
import com.rejeq.cpcam.core.ui.MorphIconTarget
import com.rejeq.cpcam.feature.main.MainNavigation.Dialog
import com.rejeq.cpcam.feature.main.camera.PreviewCameraComponent
import kotlinx.coroutines.flow.MutableStateFlow

class PreviewMainNavigation : MainNavigation {
    override val dialog: Value<ChildSlot<*, Dialog>> =
        MutableValue(ChildSlot<Any, Dialog>(null))

    override fun showStreamInfo() {}
    override fun showPermissionDenied(permission: String) {}
}

class PreviewMainComponent : MainComponent {
    override val nav = PreviewMainNavigation()
    override val cam = PreviewCameraComponent()

    override val streamButtonState = MorphButtonState(MorphIconTarget.Stopped)
    override val showStreamButton = MutableStateFlow(true)
    override val showSwitchCameraButton = MutableStateFlow(true)
    override val showInfoButton = MutableStateFlow(true)
    override val keepScreenAwake = MutableStateFlow(false)
    override val dimScreenDelay = MutableStateFlow(null)

    override fun onSettingsClick() { }

    override fun onStartEndpoint() {
        streamButtonState.animTarget = MorphIconTarget.Started
    }

    override fun onStopEndpoint() {
        streamButtonState.animTarget = MorphIconTarget.Stopped
    }

    override fun readyToShow(): Boolean = true
}
