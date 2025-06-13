package com.rejeq.cpcam.core.ui.modifier

import android.annotation.SuppressLint
import android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
import androidx.activity.compose.LocalActivity
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.platform.InspectorInfo

/**
 * A modifier that keeps the screen awake while the composable is in the
 * composition tree.
 *
 * When the composable is disposed, or this modifier is removed, the screen will
 * be allowed to sleep.
 *
 * @param enabled Whether to keep the screen awake. If false, the screen will be
 *        allowed to sleep.
 */
fun Modifier.keepScreenAwake(enabled: Boolean): Modifier = this.then(
    if (enabled) {
        KeepScreenAwakeElement
    } else {
        Modifier
    },
)

private data object KeepScreenAwakeElement :
    ModifierNodeElement<KeepScreenAwakeNode>() {
    override fun create() = KeepScreenAwakeNode()
    override fun update(node: KeepScreenAwakeNode) { }

    override fun InspectorInfo.inspectableProperties() {
        name = "keepScreenAwake"
    }
}

private class KeepScreenAwakeNode :
    Modifier.Node(),
    CompositionLocalConsumerModifierNode {

    @SuppressLint("SuspiciousCompositionLocalModifierRead")
    override fun onAttach() {
        val window = currentValueOf(LocalActivity)?.window
        window?.addFlags(FLAG_KEEP_SCREEN_ON)
    }

    @SuppressLint("SuspiciousCompositionLocalModifierRead")
    override fun onDetach() {
        val window = currentValueOf(LocalActivity)?.window
        window?.clearFlags(FLAG_KEEP_SCREEN_ON)
    }
}
