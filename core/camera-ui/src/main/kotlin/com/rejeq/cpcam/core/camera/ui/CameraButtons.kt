package com.rejeq.cpcam.core.camera.ui

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.rejeq.cpcam.core.ui.CpcamIconButton
import com.rejeq.cpcam.core.ui.R as CoreR
import com.rejeq.cpcam.core.ui.modifier.adaptiveRotation

@Composable
fun SwitchCameraButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    CpcamIconButton(onClick = onClick, modifier = modifier) {
        Icon(
            painter = painterResource(CoreR.drawable.ic_flip_camera_24dp),
            contentDescription = stringResource(R.string.flip_camera_btn_desc),
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
                    CoreR.drawable.ic_flash_on_24dp
                } else {
                    CoreR.drawable.ic_flash_off_24dp
                },
            ),
            contentDescription = stringResource(
                if (isEnabled) {
                    R.string.torch_on_btn_desc
                } else {
                    R.string.torch_off_btn_desc
                },
            ),
            modifier = Modifier.adaptiveRotation(),
        )
    }
}
