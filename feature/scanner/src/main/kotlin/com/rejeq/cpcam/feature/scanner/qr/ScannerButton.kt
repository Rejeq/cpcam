package com.rejeq.cpcam.feature.scanner.qr

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewDynamicColors
import com.rejeq.cpcam.core.ui.R as CoreR
import com.rejeq.cpcam.core.ui.theme.CpcamTheme
import com.rejeq.cpcam.feature.scanner.R
import kotlinx.coroutines.delay

@Composable
fun ScannerButton(
    state: ScannerButtonState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val transition = updateTransition(
        targetState = state,
        label = "ScannerButtonTransition",
    )

    val bgColor by transition.animateColor(
        label = "BackgroundColor",
        transitionSpec = { ANIMATION_SPEC },
    ) {
        when (it) {
            ScannerButtonState.Failed ->
                MaterialTheme.colorScheme.error
            else ->
                MaterialTheme.colorScheme.primary
        }
    }

    val fgColor by transition.animateColor(
        label = "ForegroundColor",
        transitionSpec = { ANIMATION_SPEC },
    ) {
        when (it) {
            ScannerButtonState.Failed ->
                MaterialTheme.colorScheme.onError
            else ->
                MaterialTheme.colorScheme.onPrimary
        }
    }

    FilledIconButton(
        onClick = onClick,
        enabled = enabled,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = bgColor,
            contentColor = fgColor,
            disabledContainerColor = bgColor,
            disabledContentColor = fgColor,
        ),
        modifier = modifier,
    ) {
        Icon(
            painter = painterResource(CoreR.drawable.ic_search_24dp),
            contentDescription = stringResource(R.string.scanner_btn_desc),
            modifier = Modifier.fillMaxSize(ICON_SIZE_PERCENTS),
        )
    }
}

enum class ScannerButtonState {
    Analyzing,
    Failed,
}

@Preview
@PreviewDynamicColors
@Composable
private fun AnalyzingScannerButtonPreview() {
    CpcamTheme {
        ScannerButton(
            state = ScannerButtonState.Analyzing,
            onClick = {},
            enabled = true,
        )
    }
}

@Preview
@PreviewDynamicColors
@Composable
private fun FailedScannerButtonPreview() {
    CpcamTheme {
        ScannerButton(
            state = ScannerButtonState.Failed,
            onClick = {},
            enabled = true,
        )
    }
}

@Preview
@Composable
private fun AnimatedScannerButtonPreview() {
    CpcamTheme {
        var state by remember { mutableStateOf(ScannerButtonState.Analyzing) }

        LaunchedEffect(Unit) {
            while (true) {
                delay(TRANSITION_DURATION * PREVIEW_ANIM_COUNT_DELAY)

                state = if (state == ScannerButtonState.Analyzing) {
                    ScannerButtonState.Failed
                } else {
                    ScannerButtonState.Analyzing
                }
            }
        }

        ScannerButton(
            state = state,
            onClick = {},
            enabled = true,
        )
    }
}

private val ANIMATION_SPEC = tween<Color>(
    durationMillis = TRANSITION_DURATION,
    easing = FastOutSlowInEasing,
)

private const val TRANSITION_DURATION: Int = 400
private const val PREVIEW_ANIM_COUNT_DELAY: Long = 3L
private const val ICON_SIZE_PERCENTS = 0.8f
