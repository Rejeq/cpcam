package com.rejeq.cpcam.feature.settings.endpoint.form.field

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import com.rejeq.cpcam.feature.settings.endpoint.form.FIELD_ERROR_DELAY
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class IntegerFieldState(initValue: Int? = null) {
    private var errorJob: Job? = null
    var error by mutableStateOf<IntegerErrorKind?>(null)

    var value by mutableStateOf(
        TextFieldValue(
            initValue?.toString() ?: "",
        ),
    )
    val state by derivedStateOf { value.text.toIntOrNull() }

    fun onValueChange(scope: CoroutineScope, newValue: TextFieldValue) {
        value = newValue

        errorJob?.cancel()
        errorJob = scope.launch {
            delay(FIELD_ERROR_DELAY.takeIf { error == null } ?: 0L)
            updateErrors(value)
        }
    }

    fun updateErrors(state: TextFieldValue) {
        val text = state.text

        if (text.isBlank()) {
            error = null
            return
        }

        try {
            val portNumber = text.toInt()
            error = when {
                portNumber < 0 -> IntegerErrorKind.Negative
                else -> null
            }
        } catch (e: NumberFormatException) {
            error = IntegerErrorKind.NotValid
        }
    }
}

enum class IntegerErrorKind {
    Negative,
    NotValid,
}
