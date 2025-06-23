package com.rejeq.cpcam.feature.settings.preference

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import com.rejeq.cpcam.feature.settings.R
import com.rejeq.cpcam.feature.settings.item.TextItem

@Immutable
data class BackgroundState(val onDisableBOClick: () -> Unit)

fun backgroundPreferences(state: BackgroundState): List<PreferenceContent> =
    buildList {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            add { modifier ->
                DisableOptimizationPreference(
                    onClick = state.onDisableBOClick,
                    modifier = modifier,
                )
            }
        }

        add { modifier ->
            DontKillMyAppPreference(modifier)
        }
    }

@RequiresApi(Build.VERSION_CODES.M)
@Composable
fun DisableOptimizationPreference(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextItem(
        title = stringResource(R.string.pref_disable_bo_title),
        subtitle = stringResource(R.string.pref_disable_bo_desc),
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
fun DontKillMyAppPreference(modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current

    TextItem(
        title = stringResource(R.string.pref_dont_kill_my_app_title),
        subtitle = stringResource(R.string.pref_dont_kill_my_app_desc),
        onClick = { uriHandler.openUri("https://dontkillmyapp.com/") },
        modifier = modifier,
    )
}
