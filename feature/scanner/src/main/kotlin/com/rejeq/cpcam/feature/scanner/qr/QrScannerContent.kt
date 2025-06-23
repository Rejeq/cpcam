package com.rejeq.cpcam.feature.scanner.qr

import android.content.pm.ActivityInfo
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewDynamicColors
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.rejeq.cpcam.core.camera.ui.CameraContent
import com.rejeq.cpcam.core.camera.ui.HandleCameraTargetLifecycle
import com.rejeq.cpcam.core.camera.ui.SwitchCameraButton
import com.rejeq.cpcam.core.camera.ui.TorchButton
import com.rejeq.cpcam.core.common.decodeBitmapFromUri
import com.rejeq.cpcam.core.ui.Edge
import com.rejeq.cpcam.core.ui.PermissionBlockedContent
import com.rejeq.cpcam.core.ui.SlideFromEdge
import com.rejeq.cpcam.core.ui.SnackbarDispatcher
import com.rejeq.cpcam.core.ui.SnackbarDispatcherContent
import com.rejeq.cpcam.core.ui.modifier.DeviceOrientation
import com.rejeq.cpcam.core.ui.modifier.ProvideDeviceOrientation
import com.rejeq.cpcam.core.ui.modifier.adaptiveRotation
import com.rejeq.cpcam.core.ui.modifier.lockOrientation
import com.rejeq.cpcam.core.ui.theme.CpcamTheme

@Composable
fun QrScannerContent(
    component: QrScannerComponent,
    modifier: Modifier = Modifier,
    snackbarDispatcher: SnackbarDispatcher? = null,
    showPhotoPickerButton: MutableState<Boolean> =
        remember { mutableStateOf(false) },
) {
    val dialog = component.nav.dialog.subscribeAsState().value
    val dialogInstance = dialog.child?.instance

    dialogInstance?.let {
        when (it) {
            is QrScannerNavigation.Dialog.PermissionBlocked ->
                PermissionBlockedContent(it.component)
        }
    }

    val ctx = LocalContext.current
    val pickMedia = rememberLauncherForActivityResult(
        contract = PickVisualMedia(),
    ) {
        it?.let { uri ->
            Log.d(TAG, "Selected URI: $uri")

            decodeBitmapFromUri(ctx.contentResolver, uri)
                ?.let(component::analyzeBitmap)
                ?: Log.e(TAG, "Unable to decode bitmap")
        }
    }

    HandleCameraTargetLifecycle(component.qrAnalyzer)

    QrScannerScreenLayout(
        modifier = modifier,
        background = {
            val camState = component.cam.state.collectAsState().value

            ScannerOverlay(
                size = camState.size.takeIf { camState.request != null },
            ) {
                CameraContent(component.cam)
            }
        },
        top = {
            InfoBar(
                hasTorch = component.cam.hasTorch.collectAsState().value,
                isTorchEnabled = component.cam.isTorchEnabled
                    .collectAsState().value,
                onTorchClick = component.cam::onToggleTorch,
                onCloseClick = component::onFinished,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        bottom = { bottomModifier ->
            val scanState = component.scanState.collectAsState().value

            LaunchedEffect(Unit) {
                showPhotoPickerButton.value = true
            }

            ActionBar(
                scanButtonState = scanState,
                showSwitchCameraButton = component.cam.hasMultipleCameras
                    .collectAsState().value,
                onSwitchCameraClick = component.cam::onSwitchCamera,
                showPhotoPickerButton = showPhotoPickerButton.value,
                onPhotoPickerClick = {
                    pickMedia.launch(
                        PickVisualMediaRequest(PickVisualMedia.ImageOnly),
                    )
                },
                modifier = bottomModifier.fillMaxWidth(),
            )
        },
        snackbar = {
            SnackbarDispatcherContent(snackbarDispatcher)
        },
    )
}

@Composable
fun QrScannerScreenLayout(
    background: @Composable () -> Unit,
    top: @Composable () -> Unit,
    bottom: @Composable (Modifier) -> Unit,
    snackbar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val actionBarPadding = 56.dp

    ProvideDeviceOrientation(DeviceOrientation.Portrait) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .lockOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT),
        ) {
            background()

            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(16.dp)
                    .matchParentSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start,
            ) {
                top()

                Column {
                    snackbar()
                    bottom(Modifier.padding(bottom = actionBarPadding))
                }
            }
        }
    }
}

@Composable
fun InfoBar(
    hasTorch: Boolean,
    isTorchEnabled: Boolean,
    onTorchClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CloseButton(onClick = onCloseClick)

        SlideFromEdge(
            visible = hasTorch,
            edge = Edge.Top,
        ) {
            TorchButton(
                isEnabled = isTorchEnabled,
                onClick = onTorchClick,
            )
        }
    }
}

@Composable
fun ActionBar(
    scanButtonState: ScannerButtonState,
    showSwitchCameraButton: Boolean,
    onSwitchCameraClick: () -> Unit,
    showPhotoPickerButton: Boolean,
    onPhotoPickerClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rowHeight = 96.dp

    Row(
        modifier = modifier.requiredHeight(rowHeight),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.requiredSize(48.dp)) {
            SlideFromEdge(showPhotoPickerButton, Edge.Start) {
                PhotoPickerButton(onClick = onPhotoPickerClick)
            }
        }

        ScannerButton(
            state = scanButtonState,
            onClick = {},
            enabled = false,
            modifier = Modifier.requiredSize(rowHeight).adaptiveRotation(),
//            contentPadding = PaddingValues(16.dp),
        )

        Box(modifier = Modifier.requiredSize(48.dp)) {
            SlideFromEdge(showSwitchCameraButton, Edge.End) {
                SwitchCameraButton(onClick = onSwitchCameraClick)
            }
        }
    }
}

@Composable
@Preview
@PreviewScreenSizes
@PreviewLightDark
@PreviewDynamicColors
private fun PreviewQrScannerContent() {
    CpcamTheme {
        Surface {
            QrScannerContent(
                component = PreviewQrScannerComponent(),
                modifier = Modifier.fillMaxSize(),
                showPhotoPickerButton = remember { mutableStateOf(true) },
            )
        }
    }
}

private const val TAG = "QrScannerContent"
