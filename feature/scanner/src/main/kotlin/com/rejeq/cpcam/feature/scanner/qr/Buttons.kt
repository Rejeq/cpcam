package com.rejeq.cpcam.feature.scanner.qr

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.rejeq.cpcam.core.ui.CpcamIconButton
import com.rejeq.cpcam.core.ui.R as CoreR
import com.rejeq.cpcam.core.ui.modifier.adaptiveRotation
import com.rejeq.cpcam.feature.scanner.R

@Composable
fun PhotoPickerButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    CpcamIconButton(onClick = onClick, modifier = modifier) {
        Icon(
            painter = painterResource(
                CoreR.drawable.ic_photo_library_24dp,
            ),
            contentDescription = stringResource(R.string.photo_picker_btn_desc),
            modifier = Modifier.adaptiveRotation(),
        )
    }
}

@Composable
fun CloseButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    CpcamIconButton(onClick = onClick, modifier = modifier) {
        Icon(
            painter = painterResource(
                CoreR.drawable.ic_arrow_back_24dp,
            ),
            contentDescription = stringResource(CoreR.string.back_btn_desc),
            // Note: Do not use adaptiveRotation modifier, since arrow has
            // specific direction
        )
    }
}
