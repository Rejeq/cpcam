package com.rejeq.cpcam.feature.main.info

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.rejeq.cpcam.feature.main.R

@Composable
fun InfoContent(component: InfoComponent, modifier: Modifier = Modifier) =
    Dialog(onDismissRequest = component.onFinished) {
        StreamInfo(
            titleText = "TODO:",
            subtitleText = "TODO:",
            onClick = { Log.i("LOGITS", "TODO:") },
            modifier = modifier,
        )
    }

@Composable
fun StreamInfo(
    titleText: String,
    subtitleText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
) {
    val roundSize = 100.dp

    Surface(
        modifier = modifier.clickable(onClick = onClick, role = Role.Button),
        shape = RoundedCornerShape(roundSize),
        color = color,
        contentColor = contentColor,
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .padding(start = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = titleText, fontWeight = FontWeight.SemiBold)
                Text(text = subtitleText, fontWeight = FontWeight.Normal)
            }

            Box(
                modifier = Modifier.minimumInteractiveComponentSize(),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_sensors_24dp),
                    contentDescription =
                    stringResource(R.string.stream_info_desc),
                )
            }
        }
    }
}
