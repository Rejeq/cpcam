package com.rejeq.cpcam.core.ui

import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults.filledTonalIconButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.rejeq.cpcam.core.ui.theme.CpcamTheme

/**
 * A styled icon button with opacity.
 *
 * @param onClick Callback invoked when the button is clicked
 * @param modifier Optional modifier for customizing the button
 * @param colors Optional custom colors for the button
 * @param content The icon content to display
 */
@Composable
fun CpcamIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: IconButtonColors = filledTonalIconButtonColors(),
    content: @Composable () -> Unit,
) {
    val transparentColors =
        colors.copy(
            containerColor = colors.containerColor.copy(alpha = OPACITY),
            disabledContainerColor =
            colors.disabledContainerColor.copy(
                alpha = OPACITY,
            ),
        )

    FilledTonalIconButton(
        onClick = onClick,
        colors = transparentColors,
        modifier = modifier,
    ) {
        content()
    }
}

@Preview
@Composable
fun PreviewCpcamIconButton() {
    CpcamTheme {
        CpcamIconButton(onClick = {}) {
            Icon(
                painter = painterResource(R.drawable.ic_help_24dp),
                contentDescription = null,
            )
        }
    }
}

private const val OPACITY = 0.4f
