package com.rejeq.cpcam.core.camera.target

import androidx.camera.core.SurfaceRequest
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.flow.StateFlow

/**
 * Represents a target for a camera, providing access to surface requests and
 * camera events.
 */
interface CameraTarget {
    val surfaceRequest: StateFlow<SurfaceRequestState>

    fun start()

    fun stop()
}

sealed interface SurfaceRequestState {
    /**
     * Represents the state where a [SurfaceRequest] has been stopped or not
     * started yet.
     *
     * This state indicates that the [SurfaceRequest] is not available, likely
     * due to a change in application needs or lifecycle events.
     */
    object Stopped : SurfaceRequestState

    /**
     * Represents the state where a [SurfaceRequest] is available and ready to
     * be used.
     *
     * This state indicates that a surface request has been successfully
     * processed and is now available for the client to interact with
     * (e.g., to provide a Surface or release the request).
     *
     * @property value The [SurfaceRequest] that is available. This represents
     *           the actual request object that can be used to provide a
     *           surface.
     */
    class Available(val value: SurfaceRequest) : SurfaceRequestState
}

/**
 * Creates a [LifecycleEventObserver] that manages the start and stop of a
 * [CameraTarget] based on the lifecycle events of an associated lifecycle
 * owner.
 *
 * This observer will call the start() of the [CameraTarget] when the
 * lifecycle moves to the [Lifecycle.Event.ON_START] state, and it will call the
 * stop() when the lifecycle moves to the [Lifecycle.Event.ON_STOP]
 * state. Other lifecycle events are ignored.
 *
 * @return A [LifecycleEventObserver] that automatically starts and stops the
 *         camera target based on lifecycle events.
 */
fun CameraTarget.lifecycleObserver() = LifecycleEventObserver { _, event ->
    when (event) {
        Lifecycle.Event.ON_START -> start()
        Lifecycle.Event.ON_STOP -> stop()
        else -> { }
    }
}
