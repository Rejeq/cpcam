package com.rejeq.cpcam.feature.settings.preference

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.util.fastForEachIndexed
import com.rejeq.cpcam.core.data.model.ThemeConfig
import com.rejeq.cpcam.core.device.APPLICATION_LOCALES
import com.rejeq.cpcam.core.device.Locale
import com.rejeq.cpcam.core.ui.isFollowDarkModeSupported
import com.rejeq.cpcam.core.ui.theme.isDynamicThemingSupported
import com.rejeq.cpcam.feature.settings.R
import com.rejeq.cpcam.feature.settings.item.DialogSelectableRow
import com.rejeq.cpcam.feature.settings.item.ListDialogItem
import com.rejeq.cpcam.feature.settings.item.SwitchItem
import kotlinx.coroutines.flow.StateFlow

@Stable
data class AppearanceState(
    val selectedTheme: StateFlow<ThemeConfig?>,
    val onThemeChange: (newTheme: ThemeConfig) -> Unit,
    val useDynamicColor: StateFlow<Boolean?>,
    val onDynamicColorChange: (newState: Boolean) -> Unit,
    val selectedLocale: StateFlow<Locale?>,
    val onLocaleChange: (newLocale: Locale) -> Unit,
)

fun appearancePreferences(state: AppearanceState): List<PreferenceContent> =
    buildList {
        add { modifier ->
            ThemeConfigPreference(
                selected = state.selectedTheme.collectAsState().value,
                onChange = state.onThemeChange,
                modifier = modifier,
            )
        }

        if (isDynamicThemingSupported()) {
            add { modifier ->
                UseDynamicColorPreference(
                    checked = state.useDynamicColor.collectAsState().value,
                    onChange = state.onDynamicColorChange,
                    modifier = modifier,
                )
            }
        }

        add { modifier ->
            LocalePickerPreference(
                selected = state.selectedLocale.collectAsState().value,
                onChange = state.onLocaleChange,
                modifier = modifier,
            )
        }
    }

@Composable
fun ThemeConfigPreference(
    selected: ThemeConfig?,
    onChange: (newTheme: ThemeConfig) -> Unit,
    modifier: Modifier = Modifier,
) {
    val ctx = LocalContext.current

    val entries = ThemeConfig.entries.filter {
        when (it) {
            ThemeConfig.FOLLOW_SYSTEM -> ctx.isFollowDarkModeSupported()
            else -> true
        }
    }

    val showDialog = rememberSaveable { mutableStateOf(false) }

    ListDialogItem(
        title = stringResource(R.string.pref_theme_title),
        subtitle = stringResource(R.string.pref_theme_desc),
        selected = selected?.toStringResource(),
        isDialogShown = showDialog.value,
        onDialogDismiss = { showDialog.value = false },
        onItemClick = { showDialog.value = true },
        modifier = modifier,
    ) {
        entries.fastForEachIndexed { idx, entry ->
            item {
                DialogSelectableRow(
                    label = entry.toStringResource(),
                    isSelected = (entries.indexOf(selected) == idx),
                    onSelect = {
                        onChange(entries[idx])
                        showDialog.value = false
                    },
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

@Composable
fun LocalePickerPreference(
    selected: Locale?,
    onChange: (newLocale: Locale) -> Unit,
    modifier: Modifier = Modifier,
) {
    val entries = APPLICATION_LOCALES
    val showDialog = rememberSaveable { mutableStateOf(false) }

    ListDialogItem(
        title = stringResource(R.string.pref_language_title),
        subtitle = stringResource(R.string.pref_language_desc),
        selected = null,
        isDialogShown = showDialog.value,
        onDialogDismiss = { showDialog.value = false },
        onItemClick = { showDialog.value = true },
        modifier = modifier,
    ) {
        entries.forEach { locale ->
            item {
                DialogSelectableRow(
                    label = stringResource(locale.labelId),
                    isSelected = selected?.tag == locale.tag,
                    onSelect = {
                        onChange(locale)
                        showDialog.value = false
                    },
                )
            }
        }
    }
}

@Composable
@ReadOnlyComposable
private fun ThemeConfig.toStringResource(): String = when (this) {
    ThemeConfig.FOLLOW_SYSTEM ->
        stringResource(R.string.pref_theme_follow_system)
    ThemeConfig.LIGHT ->
        stringResource(R.string.pref_theme_light)
    ThemeConfig.DARK ->
        stringResource(R.string.pref_theme_dark)
}
