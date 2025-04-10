package com.rejeq.cpcam.feature.main

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.PointerInputModifierNode
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A modifier that detects user activity and notifies when the user becomes
 * active or inactive.
 * The user is considered inactive if no touch events occur within the specified
 * [inactivityDelay].
 *
 * @param enabled Whether the activity detection is enabled
 * @param inactivityDelay The time in milliseconds after which the user is
 *        considered inactive
 * @param pollTime The time to wait to check whether the user has performed
 *        activity
 * @param onActivityChange Callback that is called when user activity state
 *        changes
 */
fun Modifier.detectUserActivity(
    enabled: Boolean,
    inactivityDelay: Long,
    pollTime: Long = DEFAULT_POLL_TIME,
    onActivityChange: (isActive: Boolean) -> Unit,
): Modifier = this then UserActivityDetectorElement(
    enabled = enabled,
    inactivityDelay = inactivityDelay,
    pollTime = pollTime,
    onActivityChange = onActivityChange,
)

private data class UserActivityDetectorElement(
    private val enabled: Boolean,
    private val inactivityDelay: Long,
    private val pollTime: Long,
    private val onActivityChange: (isActive: Boolean) -> Unit,
) : ModifierNodeElement<UserActivityDetectorNode>() {
    override fun create(): UserActivityDetectorNode = UserActivityDetectorNode(
        enabled = enabled,
        inactivityDelay = inactivityDelay,
        pollTime = pollTime,
        onActivityChange = onActivityChange,
    )

    override fun update(node: UserActivityDetectorNode) {
        node.update(enabled, inactivityDelay, pollTime, onActivityChange)
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "detectUserActivity"
        properties["enabled"] = enabled
        properties["inactivityDelay"] = inactivityDelay
        properties["pollTime"] = pollTime
    }
}

private class UserActivityDetectorNode(
    private var enabled: Boolean,
    private var inactivityDelay: Long,
    private var pollTime: Long,
    private var onActivityChange: (isActive: Boolean) -> Unit,
) : Modifier.Node(),
    PointerInputModifierNode {
    private var lastInteractionTime = System.currentTimeMillis()
    private var isActive = true
    private var activityCheckJob: Job? = null

    override fun onAttach() {
        super.onAttach()
        if (enabled) {
            startActivityCheck()
        }
    }

    override fun onDetach() {
        super.onDetach()
        activityCheckJob?.cancel()
    }

    override fun onPointerEvent(
        pointerEvent: PointerEvent,
        pass: PointerEventPass,
        bounds: IntSize,
    ) {
        if (!enabled) return

        if (pointerEvent.type == PointerEventType.Press) {
            lastInteractionTime = System.currentTimeMillis()
            if (!isActive) {
                isActive = true
                onActivityChange(true)
            }
        }
    }

    override fun onCancelPointerInput() {}

    private fun startActivityCheck() {
        activityCheckJob = coroutineScope.launch {
            while (true) {
                delay(pollTime)
                if (!enabled) continue

                val delta = System.currentTimeMillis() - lastInteractionTime
                if (delta > inactivityDelay && isActive) {
                    isActive = false
                    onActivityChange(false)
                }
            }
        }
    }

    fun update(
        newEnabled: Boolean,
        newInactivityDelay: Long,
        newPollTime: Long,
        newOnActivityChange: (isActive: Boolean) -> Unit,
    ) {
        val wasEnabled = enabled
        enabled = newEnabled
        inactivityDelay = newInactivityDelay
        pollTime = newPollTime
        onActivityChange = newOnActivityChange

        if (wasEnabled != newEnabled) {
            activityCheckJob?.cancel()
            if (newEnabled) {
                startActivityCheck()
            }
        }
    }
}

private const val DEFAULT_POLL_TIME = 1000L
