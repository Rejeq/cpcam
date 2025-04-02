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
import com.rejeq.cpcam.core.data.model.Resolution
import com.rejeq.cpcam.feature.settings.R
import com.rejeq.cpcam.feature.settings.item.DialogSelectableRow
import com.rejeq.cpcam.feature.settings.item.ListDialogItem
import com.rejeq.cpcam.feature.settings.item.TextItem
import kotlinx.coroutines.flow.Flow

@Stable
data class CameraState(
    val availableResolution: Flow<List<Resolution>>,
    val selectedResolution: Flow<Resolution?>,
    val onResolutionChange: (Resolution?) -> Unit,
    val onFramerateClick: () -> Unit,
)

fun cameraPreferences(state: CameraState): List<PreferenceContent> = listOf(
    { modifier ->
        ResolutionPreference(
            available = state.availableResolution.collectAsState(null).value,
            selected = state.selectedResolution.collectAsState(null).value,
            onChange = state.onResolutionChange,
            modifier = modifier,

        )
    },
    { modifier ->
        TextItem(
            title = "Framerate",
            subtitle = "Choose the framerate for the camera feed",
            onClick = state.onFramerateClick,
            modifier = modifier,
        )
    },
)

@Composable
fun ResolutionPreference(
    available: List<Resolution>?,
    selected: Resolution?,
    onChange: (Resolution?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val showDialog = rememberSaveable { mutableStateOf(false) }

    val entries = available?.mapToImmutableList {
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

        entries?.fastForEachIndexed { idx, entry ->
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
