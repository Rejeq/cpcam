package com.rejeq.cpcam.core.ui.modifier

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.PointerInputModifierNode
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.IntSize

/**
 * Clears the focus of the currently focused element when the user taps anywhere
 * on the composable, **provided that the tap event is not consumed by any child
 * composable**.
 *
 * This modifier only clears focus when the tap event is *not* consumed by any
 * composable within the hierarchy that is closer to the tap location. If a
 * composable (e.g., a Button) handles the tap event, this modifier will not
 * trigger. This behavior ensures that interactive elements can respond to taps
 * without interference from this focus-clearing behavior.
 **/
fun Modifier.clearFocusOnTap(): Modifier = this then ClearFocusOnTapElement

private data object ClearFocusOnTapElement :
    ModifierNodeElement<ClearFocusOnTapNode>() {
    override fun create() = ClearFocusOnTapNode()
    override fun update(node: ClearFocusOnTapNode) {}

    override fun InspectorInfo.inspectableProperties() {
        name = "clearFocusOnTap"
    }
}

private class ClearFocusOnTapNode :
    Modifier.Node(),
    CompositionLocalConsumerModifierNode,
    PointerInputModifierNode {
    private var wasMoved = false

    override fun onPointerEvent(
        pointerEvent: PointerEvent,
        pass: PointerEventPass,
        bounds: IntSize,
    ) {
        if (pass != PointerEventPass.Final) {
            return
        }

        when (pointerEvent.type) {
            PointerEventType.Move -> {
                wasMoved = true
            }
            PointerEventType.Release -> {
                val change = pointerEvent.changes.first()

                if (!wasMoved && !change.isConsumed) {
                    val focusManager = currentValueOf(LocalFocusManager)

                    focusManager.clearFocus()
                    change.consume()
                }

                wasMoved = false
            }
        }
    }

    override fun onCancelPointerInput() {
        wasMoved = false
    }
}
