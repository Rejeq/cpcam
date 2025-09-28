package com.rejeq.cpcam.feature.main

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.rejeq.cpcam.core.camera.ui.PreviewCameraComponent
import com.rejeq.cpcam.core.ui.MorphButtonState
import com.rejeq.cpcam.core.ui.MorphIconTarget
import com.rejeq.cpcam.feature.main.MainNavigation.Dialog
import kotlinx.coroutines.flow.MutableStateFlow

class PreviewMainNavigation : MainNavigation {
    override val dialog: Value<ChildSlot<*, Dialog>> =
        MutableValue(ChildSlot<Any, Dialog>(null))

    override fun showStreamInfo() {}
}

class PreviewMainComponent : MainComponent {
    override val nav = PreviewMainNavigation()
    override val cam = PreviewCameraComponent()

    override val streamButtonState = MorphButtonState(MorphIconTarget.Stopped)
    override val showStreamButton = MutableStateFlow(true)
    override val showInfoButton = MutableStateFlow(true)
    override val keepScreenAwake = MutableStateFlow(false)
    override val dimScreenDelay = MutableStateFlow(null)

    override fun readyToShow(): Boolean = true
}
