package com.rejeq.cpcam.feature.main.camera

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
    val infiniteTransition = rememberInfiniteTransition(label = "focus")

    val size by infiniteTransition.animateFloat(
        initialValue = 48f,
        targetValue = 52f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "size",
    )

    val color by infiniteTransition.animateColor(
        initialValue = Color.White,
        targetValue = Color.Red,
        animationSpec = infiniteRepeatable(
            animation = tween(300),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "color",
    )

    AnimatedVisibility(
        visible = state !is FocusIndicatorState.Disabled,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
            .offset { state.pos }
            .offset((-24).dp, (-24).dp)
            .size(48.dp)
            .drawWithContent {
                val currentSize = when (state) {
                    is FocusIndicatorState.Focusing -> size
                    else -> 48f
                }

                val currentColor = when (state) {
                    is FocusIndicatorState.Failed -> color
                    else -> Color.White
                }

                drawCircle(
                    color = currentColor,
                    radius = currentSize / 2,
                    style = Stroke(width = 2.dp.toPx()),
                )
            },
    ) { }
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
