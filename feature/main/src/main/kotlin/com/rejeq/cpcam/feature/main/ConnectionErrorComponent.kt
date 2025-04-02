package com.rejeq.cpcam.feature.main

import androidx.compose.runtime.Immutable
import com.arkivanov.decompose.ComponentContext
import com.rejeq.cpcam.core.endpoint.EndpointErrorKind

class ConnectionErrorComponent(
    componentContext: ComponentContext,
    val reason: EndpointErrorKind,
    val onFinished: () -> Unit,
    val openEndpointSettings: () -> Unit,
) : ComponentContext by componentContext

@Immutable
data class ConnectionError(val title: String, val desc: String)
