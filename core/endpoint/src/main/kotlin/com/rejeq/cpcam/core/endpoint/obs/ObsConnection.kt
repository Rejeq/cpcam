package com.rejeq.cpcam.core.endpoint.obs

import android.util.Log
import com.rejeq.cpcam.core.data.model.ObsConfig
import com.rejeq.cpcam.core.data.model.ObsStreamData
import com.rejeq.ktobs.ObsAuthException
import com.rejeq.ktobs.ObsEventSubs
import com.rejeq.ktobs.ObsRequestException
import com.rejeq.ktobs.ObsSession
import com.rejeq.ktobs.ktor.ObsSessionBuilder
import com.rejeq.ktobs.request.inputs.createInput
import com.rejeq.ktobs.request.inputs.getInputKindList
import com.rejeq.ktobs.request.scenes.createScene
import com.rejeq.ktobs.request.scenes.getCurrentProgramScene
import io.ktor.client.HttpClient
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.util.network.UnresolvedAddressException
import io.ktor.utils.io.CancellationException
import java.net.ConnectException
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

sealed class InputKind(val name: String) {
    class Media(val url: String, val speed: Int = 100) :
        InputKind("ffmpeg_source")
}

internal suspend fun obsConnect(
    client: HttpClient,
    config: ObsConfig,
    block: suspend ObsSession.() -> Unit,
): ObsErrorKind? = tryObsCall {
    val session = ObsSessionBuilder(client).apply {
        this.host = config.url
        this.port = config.port
        this.password = config.password
        this.eventSubs = ObsEventSubs.None
    }

    session.connect(block)
    null
}

internal suspend fun checkObsConnection(
    client: HttpClient,
    config: ObsConfig,
): ObsErrorKind? = obsConnect(
    client,
    config,
    block = {},
)

internal suspend fun ObsSession.setupObsScene(data: ObsStreamData) {
    // TODO: Do not create new one if it already created

    createStreamInput(
        name = "CameraStream",
        InputKind.Media(data.host),
    )

    // TODO: Also make sure if it is visible
}

internal suspend fun ObsSession.createStreamInput(
    name: String,
    kind: InputKind,
): ObsErrorKind? = tryObsCall {
    val kindList = getInputKindList()
    if (!kindList.contains(kind.name)) {
        Log.e(TAG, "OBS doesn't support '${kind.name}' input kind")
        return ObsErrorKind.UnknownInput(kind)
    }

    val sceneUuid = getOrCreateActiveScene()
    val settings = createInputSettings(kind)

    createInput(
        sceneUuid = sceneUuid,
        name = name,
        kind = kind.name,
        settings = settings,
    )

    null
}

internal fun createInputSettings(kind: InputKind) = buildJsonObject {
    when (kind) {
        is InputKind.Media -> {
            put("input", kind.url)
            put("is_local_file", false)
            put("speed", kind.speed)
        }
    }
}

internal suspend fun ObsSession.getOrCreateActiveScene(): String = try {
    getCurrentProgramScene().uuid
} catch (e: ObsRequestException) {
    Log.w(TAG, "Failed to create get current scene: $e")
    createFallbackScene()
}

internal suspend fun ObsSession.createFallbackScene(): String {
    val fallbackScene = "MainScene"
    Log.i(TAG, "Creating fallback scene '$fallbackScene'")

    return try {
        createScene(fallbackScene).orEmpty()
    } catch (e: ObsRequestException) {
        Log.e(TAG, "Failed to create fallback scene: $e")
        throw e
    }
}

internal suspend inline fun tryObsCall(
    block: suspend () -> ObsErrorKind?,
): ObsErrorKind? = try {
    block()
} catch (e: Exception) {
    when (e) {
        is ObsRequestException -> ObsErrorKind.RequestFailed(e)
        is ObsAuthException -> ObsErrorKind.AuthFailed(e.kind)
        is UnresolvedAddressException -> ObsErrorKind.UnknownHost
        is ConnectTimeoutException -> ObsErrorKind.ConnectionTimeout
        is ConnectException -> ObsErrorKind.ConnectionRefused
        is CancellationException -> throw e
        else -> ObsErrorKind.Unknown(e)
    }
}

private const val TAG = "ObsConnection"
