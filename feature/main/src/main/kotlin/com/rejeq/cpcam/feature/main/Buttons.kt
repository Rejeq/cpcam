package com.rejeq.cpcam.feature.main

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.rejeq.cpcam.core.ui.CpcamIconButton
import com.rejeq.cpcam.core.ui.adaptiveRotation

@Composable
fun SwitchCameraButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    CpcamIconButton(onClick = onClick, modifier = modifier) {
        Icon(
            painter = painterResource(R.drawable.ic_flip_camera_24dp),
            contentDescription = stringResource(
                R.string.btn_flip_camera_desc,
            ),
            modifier = Modifier.adaptiveRotation(),
        )
    }
}

@Composable
fun HelpButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    CpcamIconButton(onClick = onClick, modifier = modifier) {
        Icon(
            painter = painterResource(R.drawable.ic_help_24dp),
            contentDescription = stringResource(R.string.btn_help_desc),
            modifier = Modifier.adaptiveRotation(),
        )
    }
}

@Composable
fun SettingsButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    CpcamIconButton(onClick = onClick, modifier = modifier) {
        Icon(
            painter = painterResource(R.drawable.ic_settings_24dp),
            contentDescription = stringResource(
                R.string.btn_settings_desc,
            ),
            modifier = Modifier.adaptiveRotation(),
        )
    }
}

@Composable
fun StreamInfoButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    CpcamIconButton(onClick = onClick, modifier = modifier) {
        Icon(
            painter = painterResource(R.drawable.ic_sensors_24dp),
            contentDescription = stringResource(
                R.string.btn_stream_info_desc,
            ),
            modifier = Modifier.adaptiveRotation(),
        )
    }
}

@Composable
fun TorchButton(
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CpcamIconButton(onClick = onClick, modifier = modifier) {
        Icon(
            painter = painterResource(
                if (isEnabled) {
                    R.drawable.ic_flash_on_24dp
                } else {
                    R.drawable.ic_flash_off_24dp
                },
            ),
            contentDescription = stringResource(
                if (isEnabled) {
                    R.string.btn_torch_on_desc
                } else {
                    R.string.btn_torch_off_desc
                },
            ),
            modifier = Modifier.adaptiveRotation(),
        )
    }
}
