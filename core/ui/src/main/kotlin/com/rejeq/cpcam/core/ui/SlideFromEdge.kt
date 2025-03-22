package com.rejeq.cpcam.core.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection

/**
 * An animated composable that slides its content in and out from a specified
 * edge. The animation direction is automatically adjusted for right-to-left
 * (RTL) layouts.
 *
 * @param visible Controls the visibility of the content.
 *        If `true`, the content is shown;
 *        otherwise, it's hidden.
 * @param edge The edge from which the content should slide in and out.
 * @param modifier Modifier to be applied to the [AnimatedVisibility].
 * @param content The composable content to be animated.
 */
@Composable
fun SlideFromEdge(
    visible: Boolean,
    edge: Edge,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    val direction = LocalLayoutDirection.current
    var edge = edge

    if (direction == LayoutDirection.Rtl) {
        edge = when (edge) {
            Edge.Start -> Edge.End
            Edge.End -> Edge.Start
            else -> edge
        }
    }

    val (enter, exit) = when (edge) {
        Edge.Start -> Pair(
            slideInHorizontally { -it } + fadeIn(),
            slideOutHorizontally { -it } + fadeOut(),
        )
        Edge.End -> Pair(
            slideInHorizontally { it } + fadeIn(),
            slideOutHorizontally { it } + fadeOut(),
        )
        Edge.Top -> Pair(
            slideInVertically { -it } + fadeIn(),
            slideOutVertically { -it } + fadeOut(),
        )
        Edge.Bottom -> Pair(
            slideInVertically { it } + fadeIn(),
            slideOutVertically { it } + fadeOut(),
        )
    }

    AnimatedVisibility(
        visible = visible,
        enter = enter,
        exit = exit,
        modifier = modifier,
        content = content,
    )
}

enum class Edge {
    Start,
    End,
    Top,
    Bottom,
}

@Preview
@Composable
private fun PreviewSlideFromEdge() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CommonSlideFromEdgePreview(Edge.Top)
        CommonSlideFromEdgePreview(Edge.Start)
        CommonSlideFromEdgePreview(Edge.End)
        CommonSlideFromEdgePreview(Edge.Bottom)
    }
}

@Preview
@Composable
private fun PreviewSlideFromEdgeRtl() {
    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CommonSlideFromEdgePreview(Edge.Start)
            CommonSlideFromEdgePreview(Edge.End)
        }
    }
}

@Composable
private fun CommonSlideFromEdgePreview(edge: Edge) {
    SlideFromEdge(true, edge) {
        Button(onClick = { }) {
            Text(edge.name)
        }
    }
}
