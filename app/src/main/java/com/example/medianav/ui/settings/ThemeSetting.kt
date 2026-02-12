package com.example.medianav.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.medianav.config.ConfigManager
import com.example.medianav.ui.theme.Theme
import com.example.plugin_common.util.toTitleCase

@Composable
internal fun ThemeSetting(viewModel: SettingsViewModel) {
    val currentTheme by ConfigManager.theme.collectAsState()

    Setting(
        viewModel = viewModel,
        title = "App Theme",
        subtitle = currentTheme.value.toTitleCase(),
        leftIcon = Icons.Default.Palette,
        type = SettingType.THEME,
        dropdownContent = {
            ThemeDropdown(viewModel, currentTheme)
        }
    )
}

@Composable
private fun ThemeDropdown(
    viewModel: SettingsViewModel,
    currentTheme: Theme
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Theme.entries.forEach { theme ->
                ThemeItem(viewModel, currentTheme, theme)
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                )
            }
        }
    }
}

@Composable
private fun ThemeItem(
    viewModel: SettingsViewModel,
    currentTheme: Theme,
    theme: Theme
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                viewModel.setTheme(theme)
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = theme.value.toTitleCase(),
            style = MaterialTheme.typography.bodyLarge,
            color =
                if (theme == currentTheme) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
        )
    }
}