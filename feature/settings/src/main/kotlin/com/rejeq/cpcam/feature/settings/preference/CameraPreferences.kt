package com.rejeq.cpcam.feature.settings.preference

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.util.fastForEachIndexed
import com.rejeq.cpcam.core.common.mapToImmutableList
import com.rejeq.cpcam.core.data.model.Framerate
import com.rejeq.cpcam.core.data.model.Resolution
import com.rejeq.cpcam.feature.settings.R
import com.rejeq.cpcam.feature.settings.item.DialogSelectableRow
import com.rejeq.cpcam.feature.settings.item.ListDialogItem
import kotlinx.coroutines.flow.StateFlow

@Stable
data class CameraState(
    val availableResolution: StateFlow<List<Resolution>>,
    val selectedResolution: StateFlow<Resolution?>,
    val onResolutionChange: (Resolution?) -> Unit,
    val availableFramerates: StateFlow<List<Framerate>>,
    val selectedFramerate: StateFlow<Framerate?>,
    val onFramerateChange: (Framerate?) -> Unit,
)

fun cameraPreferences(state: CameraState): List<PreferenceContent> = listOf(
    { modifier ->
        ResolutionPreference(
            available = state.availableResolution.collectAsState().value,
            selected = state.selectedResolution.collectAsState().value,
            onChange = state.onResolutionChange,
            modifier = modifier,
        )
    },
    { modifier ->
        FrameratePreference(
            available = state.availableFramerates.collectAsState().value,
            selected = state.selectedFramerate.collectAsState().value,
            onChange = state.onFramerateChange,
            modifier = modifier,
        )
    },
)

@Composable
fun ResolutionPreference(
    available: List<Resolution>,
    selected: Resolution?,
    onChange: (Resolution?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val showDialog = rememberSaveable { mutableStateOf(false) }

    val entries = available.mapToImmutableList {
        val simplified = it.simplified()
        "$it (${simplified.width}/${simplified.height})"
    }

    ListDialogItem(
        title = stringResource(R.string.pref_resolution_title),
        subtitle = stringResource(R.string.pref_resolution_desc),
        selected = selected?.toString(),
        isDialogShown = showDialog.value,
        onDialogDismiss = { showDialog.value = false },
        onItemClick = { showDialog.value = true },
        modifier = modifier,
    ) {
        item {
            DialogSelectableRow(
                label = stringResource(R.string.pref_resolution_default),
                isSelected = selected == null,
                onSelect = {
                    onChange(null)
                    showDialog.value = false
                },
            )
        }

        entries.fastForEachIndexed { idx, entry ->
            item {
                DialogSelectableRow(
                    label = entry,
                    isSelected = (available.indexOf(selected) == idx),
                    onSelect = {
                        onChange(available[idx])
                        showDialog.value = false
                    },
                )
            }
        }
    }
}

@Composable
fun FrameratePreference(
    available: List<Framerate>,
    selected: Framerate?,
    onChange: (Framerate?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val showDialog = rememberSaveable { mutableStateOf(false) }

    val entries = available.mapToImmutableList { framerate ->
        framerate.toDisplayedString()
    }

    ListDialogItem(
        title = stringResource(R.string.pref_framerate_title),
        subtitle = stringResource(R.string.pref_framerate_desc),
        selected = selected?.toDisplayedString(),
        isDialogShown = showDialog.value,
        onDialogDismiss = { showDialog.value = false },
        onItemClick = { showDialog.value = true },
        modifier = modifier,
    ) {
        item {
            DialogSelectableRow(
                label = stringResource(R.string.pref_framerate_default),
                isSelected = selected == null,
                onSelect = {
                    onChange(null)
                    showDialog.value = false
                },
            )
        }

        entries.fastForEachIndexed { idx, framerate ->
            item {
                DialogSelectableRow(
                    label = framerate.toString(),
                    isSelected = available.indexOf(selected) == idx,
                    onSelect = {
                        onChange(available[idx])
                        showDialog.value = false
                    },
                )
            }
        }
    }
}

fun Framerate.toDisplayedString(): String = when {
    min == max -> "$max fps"
    else -> "$min-$max fps"
}
