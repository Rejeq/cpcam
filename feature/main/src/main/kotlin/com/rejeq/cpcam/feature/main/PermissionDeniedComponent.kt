package com.rejeq.cpcam.feature.main

import com.arkivanov.decompose.ComponentContext

class PermissionDeniedComponent(
    componentContext: ComponentContext,
    val permission: String,
    val onFinished: () -> Unit,
) : ComponentContext by componentContext
