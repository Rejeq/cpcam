package com.rejeq.cpcam.feature.settings.endpoint.form.obs

import androidx.compose.runtime.Stable
import androidx.compose.runtime.snapshotFlow
import com.rejeq.cpcam.core.data.model.ObsConfig
import com.rejeq.cpcam.feature.settings.endpoint.form.field.PasswordFieldState
import com.rejeq.cpcam.feature.settings.endpoint.form.field.PortFieldState
import com.rejeq.cpcam.feature.settings.endpoint.form.field.UrlFieldState

@Stable
class ObsConfigFormState(
    initUrl: String,
    initPassword: String,
    initPort: Int?,
) {
    val url = UrlFieldState(initUrl)

    val password = PasswordFieldState(initPassword)

    val port = PortFieldState(initPort)

    val state = snapshotFlow {
        ObsConfig(
            url = url.state,
            port = port.state ?: 0,
            password = password.state,
        )
    }
}
