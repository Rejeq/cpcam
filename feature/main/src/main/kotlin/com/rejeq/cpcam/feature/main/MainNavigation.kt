package com.rejeq.cpcam.feature.main

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.value.Value
import com.rejeq.cpcam.feature.main.info.InfoComponent
import kotlinx.serialization.Serializable

class MainNavigation(componentContext: ComponentContext) {
    private val dialogNavigation = SlotNavigation<DialogConfig>()

    val dialog: Value<ChildSlot<*, Dialog>> = componentContext.childSlot(
        source = dialogNavigation,
        serializer = DialogConfig.serializer(),
        handleBackButton = true,
    ) { config, childComponentContext ->
        when (config) {
            is DialogConfig.Info -> Dialog.Info(
                InfoComponent(
                    childComponentContext,
                    onFinished = dialogNavigation::dismiss,
                ),
            )

            is DialogConfig.PermissionDenied ->
                Dialog.PermanentNotification(
                    PermissionDeniedComponent(
                        childComponentContext,
                        config.permissions,
                        onFinished = dialogNavigation::dismiss,
                    ),
                )
        }
    }

    fun showStreamInfo() {
        dialogNavigation.activate(DialogConfig.Info)
    }

    fun showPermissionDenied(permission: String) {
        dialogNavigation.activate(
            DialogConfig.PermissionDenied(permission),
        )
    }

    @Serializable
    private sealed interface DialogConfig {
        @Serializable
        data object Info : DialogConfig

        @Serializable
        data class PermissionDenied(val permissions: String) : DialogConfig
    }

    sealed interface Dialog {
        data class Info(val component: InfoComponent) : Dialog

        data class PermanentNotification(
            val component: PermissionDeniedComponent,
        ) : Dialog
    }
}
