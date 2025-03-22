package com.rejeq.cpcam.core.ui

import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import com.rejeq.cpcam.core.data.model.ThemeConfig

fun Context.isFollowDarkModeSupported(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

@Composable
@ReadOnlyComposable
fun wantUseDarkMode(theme: ThemeConfig): Boolean = when (theme) {
    ThemeConfig.FOLLOW_SYSTEM -> isSystemInDarkTheme()
    ThemeConfig.LIGHT -> false
    ThemeConfig.DARK -> true
}
