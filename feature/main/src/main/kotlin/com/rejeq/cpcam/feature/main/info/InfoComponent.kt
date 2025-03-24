package com.rejeq.cpcam.feature.main.info

import androidx.compose.runtime.Stable
import com.arkivanov.decompose.ComponentContext

@Stable
class InfoComponent(
    componentContext: ComponentContext,
    val onFinished: () -> Unit,
) : ComponentContext by componentContext
