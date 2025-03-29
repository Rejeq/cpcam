package com.rejeq.cpcam.feature.main

import com.arkivanov.decompose.ComponentContext
import com.rejeq.cpcam.core.endpoint.EndpointErrorKind

class ConnectionErrorComponent(
    componentContext: ComponentContext,
    val reason: EndpointErrorKind,
    val onFinished: () -> Unit,
) : ComponentContext by componentContext
