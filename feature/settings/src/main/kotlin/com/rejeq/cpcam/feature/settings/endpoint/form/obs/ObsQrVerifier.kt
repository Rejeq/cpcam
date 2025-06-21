package com.rejeq.cpcam.feature.settings.endpoint.form.obs

import com.rejeq.cpcam.core.common.CodeVerifier
import kotlinx.serialization.Serializable

@Serializable
class ObsQrVerifier : CodeVerifier {
    override fun verifyCode(value: String): Boolean {
        val config = parseObsConfigUrl(value)
        return config != null
    }
}

// TODO: IPv6 support
internal fun parseObsConfigUrl(url: String): ObsConfigFormState? = url
    .trim()
    .takeIf { it.startsWith("obsws://") }
    ?.removePrefix("obsws://")
    ?.let { stripped ->
        runCatching {
            // split into [host:port, password]
            val (hostPort, password) = stripped
                .split("/", limit = 2)
                .also { require(it.size == 2) }

            // split host:port into [host, port]
            val (host, portStr) = hostPort
                .split(":", limit = 2)
                .also { require(it.size == 2) }

            ObsConfigFormState(
                initUrl = host,
                initPort = portStr.toInt(),
                initPassword = password,
            )
        }.getOrNull()
    }
