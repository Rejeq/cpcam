package com.rejeq.cpcam.core.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.tooling.preview.Preview
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.toPath
import com.rejeq.cpcam.core.ui.theme.CpcamTheme

@Composable
fun MorphIconButton(
    state: MorphButtonState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: MorphIconColors = MorphIconDefaults.colors(),
) {
    val currColors = if (state.isColorsInverted) {
        colors.inverted
    } else {
        colors.primary
    }

    FilledTonalIconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = currColors,
    ) {
        when (state.currAnim ?: state.animTarget) {
            MorphIconTarget.Stopped -> ClosedContent(state)
            MorphIconTarget.Loading -> ConnectingContent(state)
            MorphIconTarget.Started -> StartedContent(state)
            // TODO: MorphIconTarget.Failed
        }
    }
}

@Composable
private fun ClosedContent(state: MorphButtonState) {
    LaunchedEffect(state.animTarget) {
        state.animateTo(MorphIconTarget.Stopped)
    }

    CenteredMorph(
        getMorph = { size ->
            val shapes = state.getShapes(size)

            Morph(
                start = shapes.smallCircle,
                end = shapes.arrow,
            )
        },
        progress = state.progress,
    )
}

@Composable
private fun ConnectingContent(state: MorphButtonState) {
    LaunchedEffect(state.animTarget) {
        if (state.animTarget != MorphIconTarget.Loading) {
            state.isColorsInverted = false
        }

        state.animateTo(MorphIconTarget.Loading)

        if (state.progress.targetValue == MorphButtonState.ANIM_END) {
            state.isColorsInverted = true
        }
    }

    if (state.isAnimEnded()) {
        CircularProgressIndicator(
            modifier = Modifier.fillMaxSize(),
        )
    } else {
        CenteredMorph(
            getMorph = { size ->
                val shapes = state.getShapes(size)

                Morph(
                    start = shapes.smallCircle,
                    end = shapes.largeCircle,
                )
            },
            progress = state.progress,
        )
    }
}

@Composable
private fun StartedContent(state: MorphButtonState) {
    LaunchedEffect(state.animTarget) {
        state.animateTo(MorphIconTarget.Started)
    }

    CenteredMorph(
        getMorph = { size ->
            val shapes = state.getShapes(size)

            Morph(
                start = shapes.smallCircle,
                end = shapes.square,
            )
        },
        progress = state.progress,
    )
}

@Composable
private fun CenteredMorph(
    getMorph: (Size) -> Morph,
    progress: Animatable<Float, AnimationVector1D>,
    color: Color = MaterialTheme.colorScheme.onPrimary,
) {
    Box(
        Modifier
            .fillMaxSize()
            .drawWithCache {
                val cx = size.width / 2
                val cy = size.height / 2
                val morphPath = getMorph(size)
                    .toPath(progress = progress.value)
                    .asComposePath()

                onDrawBehind {
                    translate(left = cx, top = cy) {
                        drawPath(morphPath, color = color)
                    }
                }
            },
    )
}

object MorphIconDefaults {
    @Composable
    @ReadOnlyComposable
    fun colors(): MorphIconColors =
        MorphIconColors(primaryColors(), invertedColors())

    @Composable
    @ReadOnlyComposable
    fun primaryColors(): IconButtonColors = IconButtonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        disabledContainerColor = MaterialTheme.colorScheme.primary,
        disabledContentColor = MaterialTheme.colorScheme.onPrimary,
    )

    @Composable
    @ReadOnlyComposable
    fun invertedColors(): IconButtonColors = IconButtonColors(
        containerColor = MaterialTheme.colorScheme.onPrimary,
        contentColor = MaterialTheme.colorScheme.primary,
        disabledContainerColor = MaterialTheme.colorScheme.onPrimary,
        disabledContentColor = MaterialTheme.colorScheme.primary,
    )
}

@Preview
@Composable
private fun PreviewMorphButton() {
    CpcamTheme {
        val state = remember { MorphButtonState(MorphIconTarget.Stopped) }

        MorphIconButton(
            state = state,
            onClick = {
                state.animTarget = when (state.animTarget) {
                    MorphIconTarget.Stopped -> MorphIconTarget.Loading
                    MorphIconTarget.Loading -> MorphIconTarget.Started
                    MorphIconTarget.Started -> MorphIconTarget.Stopped
                }
            },
        )
    }
}
