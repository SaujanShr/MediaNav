package com.example.plugin_common.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class SettingsGroup(
    val title: String,
    val settings: List<Setting>
) {
    @Composable
    fun Content(startIndex: Int, expandedSettingIndex: Int?, toggleExpanded: (Int) -> Unit) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            SettingsGroupHeader(title)

            settings.forEachIndexed { index, setting ->
                val settingIndex = startIndex + index
                val expanded = expandedSettingIndex == settingIndex
                setting.Content(expanded, { toggleExpanded(settingIndex) })
            }
        }

    }
}

@Composable
private fun SettingsGroupHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(8.dp)
    )
}