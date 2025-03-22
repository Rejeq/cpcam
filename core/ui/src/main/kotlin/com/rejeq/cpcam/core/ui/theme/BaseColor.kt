package com.rejeq.cpcam.core.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

internal object BaseColor {
    val lightScheme =
        lightColorScheme(
            primary = Color(color = 0xFF415F91),
            onPrimary = Color(color = 0xFFFFFFFF),
            primaryContainer = Color(color = 0xFFD6E3FF),
            onPrimaryContainer = Color(color = 0xFF001B3E),
            secondary = Color(color = 0xFF565F71),
            onSecondary = Color(color = 0xFFFFFFFF),
            secondaryContainer = Color(color = 0xFFDAE2F9),
            onSecondaryContainer = Color(color = 0xFF131C2B),
            tertiary = Color(color = 0xFF705575),
            onTertiary = Color(color = 0xFFFFFFFF),
            tertiaryContainer = Color(color = 0xFFFAD8FD),
            onTertiaryContainer = Color(color = 0xFF28132E),
            error = Color(color = 0xFFBA1A1A),
            onError = Color(color = 0xFFFFFFFF),
            errorContainer = Color(color = 0xFFFFDAD6),
            onErrorContainer = Color(color = 0xFF410002),
            background = Color(color = 0xFFF9F9FF),
            onBackground = Color(color = 0xFF191C20),
            surface = Color(color = 0xFFF9F9FF),
            onSurface = Color(color = 0xFF191C20),
            surfaceVariant = Color(color = 0xFFE0E2EC),
            onSurfaceVariant = Color(color = 0xFF44474E),
            outline = Color(color = 0xFF74777F),
            outlineVariant = Color(color = 0xFFC4C6D0),
            scrim = Color(color = 0xFF000000),
            inverseSurface = Color(color = 0xFF2E3036),
            inverseOnSurface = Color(color = 0xFFF0F0F7),
            inversePrimary = Color(color = 0xFFAAC7FF),
        )

    val darkScheme =
        darkColorScheme(
            primary = Color(color = 0xFFAAC7FF),
            onPrimary = Color(color = 0xFF0A305F),
            primaryContainer = Color(color = 0xFF284777),
            onPrimaryContainer = Color(color = 0xFFD6E3FF),
            secondary = Color(color = 0xFFBEC6DC),
            onSecondary = Color(color = 0xFF283141),
            secondaryContainer = Color(color = 0xFF3E4759),
            onSecondaryContainer = Color(color = 0xFFDAE2F9),
            tertiary = Color(color = 0xFFDDBCE0),
            onTertiary = Color(color = 0xFF3F2844),
            tertiaryContainer = Color(color = 0xFF573E5C),
            onTertiaryContainer = Color(color = 0xFFFAD8FD),
            error = Color(color = 0xFFFFB4AB),
            onError = Color(color = 0xFF690005),
            errorContainer = Color(color = 0xFF93000A),
            onErrorContainer = Color(color = 0xFFFFDAD6),
            background = Color(color = 0xFF111318),
            onBackground = Color(color = 0xFFE2E2E9),
            surface = Color(color = 0xFF111318),
            onSurface = Color(color = 0xFFE2E2E9),
            surfaceVariant = Color(color = 0xFF44474E),
            onSurfaceVariant = Color(color = 0xFFC4C6D0),
            outline = Color(color = 0xFF8E9099),
            outlineVariant = Color(color = 0xFF44474E),
            scrim = Color(color = 0xFF000000),
            inverseSurface = Color(color = 0xFFE2E2E9),
            inverseOnSurface = Color(color = 0xFF2E3036),
            inversePrimary = Color(color = 0xFF415F91),
        )
}
