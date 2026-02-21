package com.example.medianav.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExtensionOff
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.medianav.ui.navigation.PluginViewModel
import com.example.plugin_common.plugin.MediaPlugin
import com.example.medianav.ui.shared.ConfirmDialog
import com.example.medianav.ui.shared.NoPlugin
import com.example.medianav.ui.shared.Plugin

@Composable
internal fun UninstallPlugin(
    settingsViewModel: SettingsViewModel,
    pluginViewModel: PluginViewModel
) {
    Setting(
        viewModel = settingsViewModel,
        title = "Uninstall Plugin",
        subtitle = "Select a plugin to uninstall",
        leftIcon = Icons.Outlined.ExtensionOff,
        type = SettingType.UNINSTALL,
        dropdownContent = {
            UninstallPluginDropdown(settingsViewModel, pluginViewModel)
        }
    )
}

@Composable
private fun UninstallPluginDropdown(
    settingsViewModel: SettingsViewModel,
    pluginViewModel: PluginViewModel
) {
    val plugins by pluginViewModel.plugins.collectAsState(initial = emptyList())
    var pluginToConfirm by remember { mutableStateOf<MediaPlugin?>(null) }

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
                Plugin(
                    plugin,
                    onClick = { pluginToConfirm = plugin }
                )
            }

            if (plugins.isEmpty()) {
                item {
                    NoPlugin(
                        "No installed plugins",
                        "You have no plugins to uninstall"
                    )
                }
            }
        }
    }

    pluginToConfirm?.let { plugin ->
        ConfirmDialog(
            title = "Uninstall Plugin",
            text = "Are you sure you want to uninstall \"${plugin.metadata.name}\"?",
            onConfirm = {
                settingsViewModel.uninstallPlugin(plugin)
                if (plugin.metadata.id == pluginViewModel.currentPlugin.value?.metadata?.id) {
                    pluginViewModel.clearPlugin()
                }
                pluginToConfirm = null
            },
            onDismiss = { pluginToConfirm = null }
        )
    }
}