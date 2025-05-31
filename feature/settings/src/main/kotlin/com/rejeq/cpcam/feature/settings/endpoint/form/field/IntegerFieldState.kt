package com.rejeq.cpcam.feature.settings.endpoint.form.field

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue

class IntegerFieldState(initValue: Int? = null) {
    var value by mutableStateOf(
        TextFieldValue(
            initValue?.toString() ?: "",
        ),
    )
    val state by derivedStateOf { value.text.toIntOrNull() }

    fun onValueChange(newValue: TextFieldValue) {
        value = newValue
    }
}
