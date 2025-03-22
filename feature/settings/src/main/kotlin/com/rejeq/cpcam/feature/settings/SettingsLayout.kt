package com.rejeq.cpcam.feature.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SettingsLayout(
    topBar: @Composable () -> Unit,
    content: LazyListScope.() -> Unit,
    modifier: Modifier = Modifier,
    footer: (@Composable LazyItemScope.() -> Unit)? = null,
) {
    Scaffold(
        topBar = topBar,
        modifier = modifier,
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            content()

            if (footer != null) {
                item(content = footer)
            }
        }
    }
}
