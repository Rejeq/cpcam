package com.rejeq.cpcam.feature.settings.endpoint.form.field

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@Stable
class EnumFieldState<T>(
    initAvailables: List<T>,
    initSelected: T,
    private val onSelectedChange: ((T) -> Unit)? = null,
) {
    var availables by mutableStateOf(initAvailables)

    var selected by mutableStateOf(initSelected)

    val state get() = selected

    fun onSelectedChange(value: T) {
        if (selected != value) {
            onSelectedChange?.invoke(value)
        }

        selected = value
    }

    fun onAvailablesChange(values: List<T>) {
        availables = values
    }
}
