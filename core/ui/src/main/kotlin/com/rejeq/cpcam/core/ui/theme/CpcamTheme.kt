package com.rejeq.cpcam.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun CpcamTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    useDynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        useDynamicColor && supportsDynamicTheming() -> {
            val context = LocalContext.current
            if (useDarkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }
        useDarkTheme -> BaseColor.darkScheme
        else -> BaseColor.lightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}

private fun supportsDynamicTheming() =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
