package com.rejeq.cpcam.feature.settings.endpoint.form.obs

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.input.TextFieldValue
import com.rejeq.cpcam.core.data.model.ObsConfig

@Immutable
data class ObsConfigFormState(
    val url: TextFieldValue,
    val port: TextFieldValue,
    val password: TextFieldValue,
) {
    fun toDomain(): ObsConfig = ObsConfig(
        url = url.text,
        port = port.text.toIntOrNull() ?: 0,
        password = password.text,
    )
}

fun ObsConfig.fromDomain(): ObsConfigFormState = ObsConfigFormState(
    url = TextFieldValue(url),
    port = TextFieldValue(port.toString()),
    password = TextFieldValue(password),
)
