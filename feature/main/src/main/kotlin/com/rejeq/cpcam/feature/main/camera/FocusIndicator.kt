package com.rejeq.cpcam.feature.main.camera

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

@Composable
fun FocusIndicator(state: FocusIndicatorState, modifier: Modifier = Modifier) {
    if (state !is FocusIndicatorState.Disabled) {
        val infiniteTransition = rememberInfiniteTransition(label = "focus")

        val size by infiniteTransition.animateFloat(
            initialValue = INDICATOR_SIZE,
            targetValue = INDICATOR_EXPANDED_SIZE,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    SIZE_ANIMATION_DURATION,
                    easing = LinearEasing,
                ),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "size",
        )

        val color by infiniteTransition.animateColor(
            initialValue = Color.White,
            targetValue = Color.Red,
            animationSpec = infiniteRepeatable(
                animation = tween(COLOR_ANIMATION_DURATION),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "color",
        )

        Box(
            modifier = modifier
                .offset { state.pos }
                .offset(
                    (-INDICATOR_SIZE / 2.0f).dp,
                    (-INDICATOR_SIZE / 2.0f).dp,
                )
                .size(INDICATOR_SIZE.dp)
                .drawWithContent {
                    val currentSize = when (state) {
                        is FocusIndicatorState.Focusing -> size
                        else -> INDICATOR_SIZE
                    }

                    val currentColor = when (state) {
                        is FocusIndicatorState.Failed -> color
                        else -> Color.White
                    }

                    drawCircle(
                        color = currentColor,
                        radius = currentSize / 2,
                        style = Stroke(width = INDICATOR_WIDTH.dp.toPx()),
                    )
                },
        )
    }
}

sealed interface FocusIndicatorState {
    val pos: IntOffset

    object Disabled : FocusIndicatorState {
        override val pos: IntOffset = IntOffset.Zero
    }

    data class Focusing(override val pos: IntOffset) : FocusIndicatorState
    data class Focused(override val pos: IntOffset) : FocusIndicatorState
    data class Failed(override val pos: IntOffset) : FocusIndicatorState
}

private const val INDICATOR_WIDTH = 2.0f
private const val INDICATOR_SIZE = 48.0f
private const val INDICATOR_EXPANDED_SIZE = 52.0f

private const val SIZE_ANIMATION_DURATION = 500
private const val COLOR_ANIMATION_DURATION = 300
