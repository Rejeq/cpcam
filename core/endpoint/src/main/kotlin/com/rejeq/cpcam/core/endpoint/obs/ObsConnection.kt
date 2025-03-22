package com.rejeq.cpcam.core.endpoint.obs

import android.util.Log
import com.rejeq.cpcam.core.data.model.ObsConfig
import com.rejeq.cpcam.core.data.model.ObsStreamData
import com.rejeq.cpcam.core.endpoint.EndpointResult
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
    host: String,
    port: Int,
    password: String?,
    block: suspend ObsSession.() -> Unit,
): EndpointResult<Unit> = tryObsCall {
    val session = ObsSessionBuilder(client).apply {
        this.host = host
        this.port = port
        this.password = password
        this.eventSubs = ObsEventSubs.None
    }

    session.connect(block)
    EndpointResult.Success(Unit)
}

internal suspend fun checkObsConnection(
    client: HttpClient,
    config: ObsConfig,
): EndpointResult<Unit> = obsConnect(
    client,
    config.url,
    config.port,
    config.password,
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
): EndpointResult<Unit> = tryObsCall {
    val kindList = getInputKindList()
    if (!kindList.contains(kind.name)) {
        Log.e(TAG, "OBS doesn't support '${kind.name}' input kind")
        return EndpointResult.ObsError(ObsErrorKind.UnknownInput(kind))
    }

    val sceneUuid = getOrCreateActiveScene()
    val settings = createInputSettings(kind)

    createInput(
        sceneUuid = sceneUuid,
        name = name,
        kind = kind.name,
        settings = settings,
    )

    EndpointResult.Success(Unit)
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

internal suspend inline fun <T> tryObsCall(
    block: suspend () -> EndpointResult<T>,
): EndpointResult<T> = try {
    block()
} catch (e: Exception) {
    when (e) {
        is ObsRequestException -> EndpointResult.ObsError(
            ObsErrorKind.RequestFailed(e),
        )
        is ObsAuthException -> EndpointResult.ObsError(
            ObsErrorKind.AuthFailed(e.kind),
        )
        is UnresolvedAddressException -> EndpointResult.UnknownHost()
        is ConnectTimeoutException -> EndpointResult.ConnectionTimeout()
        is ConnectException -> EndpointResult.ConnectionRefused()
        is CancellationException -> throw e
        else -> EndpointResult.UnknownError(e)
    }
}

private const val TAG = "ObsConnection"
