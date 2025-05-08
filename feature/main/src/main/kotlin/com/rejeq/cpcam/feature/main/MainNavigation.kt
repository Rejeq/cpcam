package com.rejeq.cpcam.feature.main

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.value.Value
import com.rejeq.cpcam.feature.main.DefaultMainNavigation.DialogConfig
import com.rejeq.cpcam.feature.main.MainNavigation.Dialog
import com.rejeq.cpcam.feature.main.info.InfoComponent
import kotlinx.serialization.Serializable

interface MainNavigation {
    val dialog: Value<ChildSlot<*, Dialog>>

    fun showStreamInfo()

    fun showPermissionDenied(permission: String)

    sealed interface Dialog {
        data class Info(val component: InfoComponent) : Dialog

        data class PermanentNotification(
            val component: PermissionDeniedComponent,
        ) : Dialog
    }
}

class DefaultMainNavigation(componentContext: ComponentContext) :
    MainNavigation {
    private val dialogNavigation = SlotNavigation<DialogConfig>()

    override val dialog: Value<ChildSlot<*, Dialog>> =
        componentContext.childSlot(
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

    override fun showStreamInfo() {
        dialogNavigation.activate(DialogConfig.Info)
    }

    override fun showPermissionDenied(permission: String) {
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
}
