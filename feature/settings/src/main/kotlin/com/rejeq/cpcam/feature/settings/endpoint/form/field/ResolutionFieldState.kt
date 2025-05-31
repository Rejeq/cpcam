package com.rejeq.cpcam.feature.settings.endpoint.form.field

import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import com.rejeq.cpcam.core.data.model.Resolution
import com.rejeq.cpcam.feature.settings.endpoint.form.FIELD_ERROR_DELAY
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Stable
class ResolutionFieldState(initRes: Resolution?) {
    private var errorJob: Job? = null
    var widthError by mutableStateOf<ResolutionErrorKind?>(null)
    var heightError by mutableStateOf<ResolutionErrorKind?>(null)

    var width by mutableStateOf(
        TextFieldValue(
            initRes?.width?.toString() ?: "",
        ),
    )
    var height by mutableStateOf(
        TextFieldValue(
            initRes?.height?.toString() ?: "",
        ),
    )

    val state by derivedStateOf {
        val w = width.text.toIntOrNull()?.takeIf { it >= 0 }
        val h = height.text.toIntOrNull()?.takeIf { it >= 0 }

        if (w == null || h == null) {
            return@derivedStateOf null
        }

        Resolution(w, h)
    }

    init {
        updateErrors(width, height)
    }

    fun onWidthChange(scope: CoroutineScope, w: TextFieldValue) {
        width = w

        onValueChange(scope, w, height)
    }

    fun onHeightChange(scope: CoroutineScope, h: TextFieldValue) {
        height = h

        onValueChange(scope, width, h)
    }

    fun onValueChange(
        scope: CoroutineScope,
        w: TextFieldValue,
        h: TextFieldValue,
    ) {
        errorJob?.cancel()
        errorJob = scope.launch {
            delay(
                FIELD_ERROR_DELAY.takeIf {
                    widthError == null && heightError == null
                } ?: 0L,
            )

            updateErrors(w, h)
        }
    }

    fun updateErrors(w: TextFieldValue, h: TextFieldValue) {
        widthError = if (w.text.toIntOrNull()?.let { it < 0 } == true) {
            ResolutionErrorKind.Negative
        } else {
            null
        }

        heightError = if (h.text.toIntOrNull()?.let { it < 0 } == true) {
            ResolutionErrorKind.Negative
        } else {
            null
        }
    }
}

enum class ResolutionErrorKind {
    Negative,
}
