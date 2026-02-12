package com.example.plugin_common.plugin.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

@Composable
fun AttributeContent(title: String?, attributes: List<String>) {
    if (title != null) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }

    for (attribute in attributes) {
        Text(
            text = attribute,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}