package com.rejeq.cpcam.core.camera.target

import androidx.camera.core.MeteringPoint
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.flow.StateFlow

/**
 * Represents a target for a camera, providing access to surface requests and
 * camera events.
 */
interface CameraTarget<T> {
    val request: StateFlow<CameraRequestState<T>>

    fun start()

    fun stop()

    fun getPoint(x: Float, y: Float): MeteringPoint? = null
}

sealed interface CameraRequestState<out T> {
    /**
     * Represents the state where a [CameraTarget] has been stopped or not
     * started yet.
     */
    object Stopped : CameraRequestState<Nothing>

    /**
     * Represents the state where a request data is available and
     * ready to be used.
     *
     * This state indicates that a request has been successfully
     * processed and is now available for the client to interact with
     * (e.g., to provide a Surface or release the request).
     *
     * @property value The data that is available.
     */
    @JvmInline
    value class Available<T>(val value: T) : CameraRequestState<T>
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
fun CameraTarget<*>.lifecycleObserver() = LifecycleEventObserver { _, event ->
    when (event) {
        Lifecycle.Event.ON_START -> start()
        Lifecycle.Event.ON_STOP -> stop()
        else -> { }
    }
}
