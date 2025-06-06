package com.rejeq.cpcam.feature.settings.endpoint.form.field

import androidx.compose.runtime.Stable
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

@Stable
class PortFieldState(initValue: Int? = null) {
    private var errorJob: Job? = null
    var error by mutableStateOf<PortErrorKind?>(null)

    var value by mutableStateOf(
        TextFieldValue(
            initValue?.toString() ?: "",
        ),
    )
    val state by derivedStateOf { value.text.toIntOrNull() }

    init {
        updateErrors(value)
    }

    fun onPortChange(scope: CoroutineScope, newValue: TextFieldValue) {
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
                portNumber < 0 -> PortErrorKind.Negative
                portNumber > PORT_MAX_VALUE -> PortErrorKind.TooBig
                else -> null
            }
        } catch (e: NumberFormatException) {
            error = PortErrorKind.NotValid
        }
    }
}

enum class PortErrorKind {
    Negative,
    TooBig,
    NotValid,
}

private const val PORT_MAX_VALUE = 65535
