package com.rejeq.cpcam.feature.scanner.qr

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.value.Value
import com.rejeq.cpcam.core.ui.PermissionBlockedComponent
import com.rejeq.cpcam.feature.scanner.qr.QrScannerNavigation.Dialog
import kotlinx.serialization.Serializable

interface QrScannerNavigation {
    val dialog: Value<ChildSlot<*, Dialog>>
    fun showPermissionBlocked(permission: String)

    sealed interface Dialog {
        data class PermissionBlocked(
            val component: PermissionBlockedComponent,
        ) : Dialog
    }
}

class DefaultQrScannerNavigation(componentContext: ComponentContext) :
    QrScannerNavigation {
    private val dialogNavigation = SlotNavigation<DialogConfig>()

    override val dialog: Value<ChildSlot<*, Dialog>> =
        componentContext.childSlot(
            source = dialogNavigation,
            serializer = DialogConfig.serializer(),
            handleBackButton = true,
        ) { config, childComponentContext ->
            when (config) {
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

    override fun showPermissionBlocked(permission: String) {
        dialogNavigation.activate(
            DialogConfig.PermissionBlocked(permission),
        )
    }

    @Serializable
    private sealed interface DialogConfig {
        @Serializable
        data class PermissionBlocked(val permissions: String) : DialogConfig
    }
}
