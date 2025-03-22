package com.rejeq.cpcam.feature.settings.preference

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.util.fastForEachIndexed
import com.rejeq.cpcam.core.common.mapFilteredToImmutableList
import com.rejeq.cpcam.core.data.model.ThemeConfig
import com.rejeq.cpcam.core.ui.isFollowDarkModeSupported
import com.rejeq.cpcam.feature.settings.R
import com.rejeq.cpcam.feature.settings.item.DialogRow
import com.rejeq.cpcam.feature.settings.item.ListItem
import com.rejeq.cpcam.feature.settings.item.SwitchItem
import kotlinx.coroutines.flow.Flow

@Stable
data class AppearanceState(
    val selectedTheme: Flow<ThemeConfig?>,
    val onThemeChange: (newTheme: ThemeConfig) -> Unit,
    val useDynamicColor: Flow<Boolean?>,
    val onDynamicColorChange: (newState: Boolean) -> Unit,
)

fun appearancePreferences(state: AppearanceState): List<PreferenceContent> =
    buildList {
        add { modifier ->
            ThemeConfigPreference(
                selected = state.selectedTheme.collectAsState(null).value,
                onChange = state.onThemeChange,
                modifier = modifier,
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add { modifier ->
                UseDynamicColorPreference(
                    checked = state.useDynamicColor.collectAsState(null).value,
                    onChange = state.onDynamicColorChange,
                    modifier = modifier,
                )
            }
        }
    }

@Composable
fun ThemeConfigPreference(
    selected: ThemeConfig?,
    onChange: (newTheme: ThemeConfig) -> Unit,
    modifier: Modifier = Modifier,
) {
    val ctx = LocalContext.current

    val entries = ThemeConfig.entries.mapFilteredToImmutableList(
        filter = {
            when (it) {
                ThemeConfig.FOLLOW_SYSTEM -> ctx.isFollowDarkModeSupported()
                else -> true
            }
        },
        transform = {
            when (it) {
                ThemeConfig.FOLLOW_SYSTEM ->
                    stringResource(R.string.pref_theme_follow_system)
                ThemeConfig.LIGHT ->
                    stringResource(R.string.pref_theme_light)
                ThemeConfig.DARK ->
                    stringResource(R.string.pref_theme_dark)
            }
        },
    )

    val selectedEntry = selected?.let { entries[it.ordinal] }

    ListItem(
        title = stringResource(R.string.pref_theme_title),
        subtitle = stringResource(R.string.pref_theme_desc),
        selected = selectedEntry,
        modifier = modifier,
    ) { showDialog ->
        entries.fastForEachIndexed { idx, entry ->
            item {
                DialogRow(
                    label = entry,
                    isSelected = (entries.indexOf(selectedEntry) == idx),
                    showDialog = showDialog,
                    onSelect = { onChange(ThemeConfig.entries[idx]) },
                )
            }
        }
    }
}

@Composable
fun UseDynamicColorPreference(
    checked: Boolean?,
    onChange: (newState: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    SwitchItem(
        title = stringResource(R.string.pref_use_dynamic_color_title),
        subtitle = stringResource(R.string.pref_use_dynamic_color_title),
        checked = checked == true,
        onValueChange = onChange,
        modifier = modifier,
        enabled = checked != null,
    )
}
