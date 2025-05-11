package com.rejeq.cpcam.core.ui

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import androidx.activity.compose.LocalActivity
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.platform.InspectorInfo

/**
 * A modifier that locks the screen orientation to the specified mode.
 *
 * When this modifier is applied to a composable, it requests that the device
 * screen be locked to the given orientation. This lock is active only while
 * the composable is in the composition tree.
 *
 * When the composable is disposed, or this modifier is removed, the screen
 * orientation will be set to the [ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED]
 * state
 *
 * @param orientation The desired screen orientation mode. This should be one of
 *        the constants defined in the `ActivityInfo` class
 *        (e.g. `ActivityInfo.SCREEN_ORIENTATION_PORTRAIT`,
 *        `ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE`).
 */
fun Modifier.lockOrientation(orientation: Int): Modifier =
    this then OrientationLockElement(orientation)

private data class OrientationLockElement(private val orientation: Int) :
    ModifierNodeElement<OrientationLockNode>() {
    override fun create() = OrientationLockNode(orientation)
    override fun update(node: OrientationLockNode) { }

    override fun InspectorInfo.inspectableProperties() {
        name = "lockOrientation"
        properties["orientation"] = orientation
    }
}

private class OrientationLockNode(private val orientation: Int) :
    Modifier.Node(),
    CompositionLocalConsumerModifierNode {
    @SuppressLint("SuspiciousCompositionLocalModifierRead")
    override fun onAttach() {
        val activity = currentValueOf(LocalActivity)

        activity?.requestedOrientation = orientation
    }

    @SuppressLint("SuspiciousCompositionLocalModifierRead")
    override fun onDetach() {
        val activity = currentValueOf(LocalActivity)

        activity?.requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }
}
