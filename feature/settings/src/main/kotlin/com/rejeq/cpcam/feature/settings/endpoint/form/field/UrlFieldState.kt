package com.rejeq.cpcam.feature.settings.endpoint.form.field

import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue

@Stable
class UrlFieldState(initValue: String) {
    var url by mutableStateOf(TextFieldValue(initValue))
    val state by derivedStateOf { url.text }

    fun onUrlChange(value: TextFieldValue) {
        url = value
    }
}
