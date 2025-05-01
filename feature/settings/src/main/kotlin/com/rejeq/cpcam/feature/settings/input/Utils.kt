package com.rejeq.cpcam.feature.settings.input

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

fun TextFieldValue.selectAll(): TextFieldValue = this.copy(
    selection = TextRange(0, text.length),
)
