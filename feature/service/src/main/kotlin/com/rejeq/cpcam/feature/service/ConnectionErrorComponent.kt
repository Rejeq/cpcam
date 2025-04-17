package com.rejeq.cpcam.feature.service

import androidx.compose.runtime.Immutable
import com.arkivanov.decompose.ComponentContext
import com.rejeq.cpcam.core.endpoint.EndpointErrorKind

interface DialogComponent {
    val onFinished: () -> Unit
}

class ConnectionErrorComponent(
    componentContext: ComponentContext,
    val reason: EndpointErrorKind,
    override val onFinished: () -> Unit,
    val openEndpointSettings: () -> Unit,
) : DialogComponent,
    ComponentContext by componentContext

@Immutable
data class ConnectionError(val title: String, val desc: String)
