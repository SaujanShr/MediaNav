package com.example.medianav.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ToggleOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.medianav.ui.navigation.PluginViewModel
import com.example.plugin_common.plugin.MediaPlugin
import com.example.medianav.ui.shared.NoPlugin
import com.example.medianav.ui.shared.Plugin

@Composable
internal fun TogglePluginSetting(
    settingsViewModel: SettingsViewModel,
    pluginViewModel: PluginViewModel
) {
    Setting(
        viewModel = settingsViewModel,
        title = "Enable Plugins",
        subtitle = "Enable or disable plugins",
        leftIcon = Icons.Outlined.ToggleOn,
        type = SettingType.TOGGLE,
        dropdownContent = {
            ToggleInstalledPluginDropdown(settingsViewModel, pluginViewModel)
        }
    )
}

@Composable
private fun ToggleInstalledPluginDropdown(
    settingsViewModel: SettingsViewModel,
    pluginViewModel: PluginViewModel
) {
    val plugins by pluginViewModel.plugins.collectAsState(initial = emptyList())
    val enabledPlugins by pluginViewModel.enabledPlugins.collectAsState(initial = emptyList())

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
        tonalElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 300.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(plugins) { plugin ->
                val enabled = enabledPlugins.contains(plugin)
                PluginItem(settingsViewModel, pluginViewModel, plugin, enabled)
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                )
            }

            if (plugins.isEmpty()) {
                item {
                    NoPlugin(
                        "No plugins enabled",
                        "You have no plugins to toggle"
                    )
                }
            }
        }
    }
}

@Composable
private fun PluginItem(
    settingsViewModel: SettingsViewModel,
    pluginViewModel: PluginViewModel,
    plugin: MediaPlugin,
    enabled: Boolean
) {
    Plugin(plugin,
        content = {
        Switch(
            checked = enabled,
            onCheckedChange = { checked ->
                settingsViewModel.setEnabled(plugin, checked)
                if (plugin.metadata.id == pluginViewModel.currentPlugin.value?.metadata?.id) {
                    pluginViewModel.clearPlugin()
                }
            }
        )
    })
}
