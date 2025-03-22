package com.rejeq.cpcam.feature.main.info

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.arkivanov.decompose.ComponentContext

@Stable
class InfoComponent(
    componentContext: ComponentContext,
    val onFinished: () -> Unit,
) : ComponentContext by componentContext
