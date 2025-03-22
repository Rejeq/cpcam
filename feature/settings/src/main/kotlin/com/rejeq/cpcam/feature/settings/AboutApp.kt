package com.rejeq.cpcam.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun AboutApp(
    versionName: String,
    onLicenseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
        )

        Text(
            text = stringResource(R.string.about_app_version, versionName),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 8.dp),
        )

        Text(
            text = stringResource(R.string.open_source_licenses),
            color = MaterialTheme.colorScheme.outline,
            style = MaterialTheme.typography.titleSmall,
            modifier =
            Modifier.clickable(onClick = onLicenseClick),
        )
    }
}
