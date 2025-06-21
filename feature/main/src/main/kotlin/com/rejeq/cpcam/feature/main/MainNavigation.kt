package com.rejeq.cpcam.feature.main

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.value.Value
import com.rejeq.cpcam.core.ui.PermissionBlockedComponent
import com.rejeq.cpcam.feature.main.MainNavigation.Dialog
import com.rejeq.cpcam.feature.main.info.InfoComponent
import kotlinx.serialization.Serializable

interface MainNavigation {
    val dialog: Value<ChildSlot<*, Dialog>>

    fun showStreamInfo()

    fun showPermissionBlocked(permission: String)

    sealed interface Dialog {
        data class Info(val component: InfoComponent) : Dialog

        data class PermissionBlocked(
            val component: PermissionBlockedComponent,
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

                is DialogConfig.PermissionBlocked ->
                    Dialog.PermissionBlocked(
                        PermissionBlockedComponent(
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

    override fun showPermissionBlocked(permission: String) {
        dialogNavigation.activate(
            DialogConfig.PermissionBlocked(permission),
        )
    }

    @Serializable
    private sealed interface DialogConfig {
        @Serializable
        data object Info : DialogConfig

        @Serializable
        data class PermissionBlocked(val permissions: String) : DialogConfig
    }
}
