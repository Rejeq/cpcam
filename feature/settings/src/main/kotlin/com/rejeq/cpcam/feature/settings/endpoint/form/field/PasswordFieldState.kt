package com.rejeq.cpcam.feature.settings.endpoint.form.field

import android.R.attr.text
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@Stable
class PasswordFieldState(initValue: String = "") {
    val password = TextFieldState(initValue)
    var state by mutableStateOf(password.text.toString())

    fun onPasswordChange(password: String) {
        state = password
    }
}
