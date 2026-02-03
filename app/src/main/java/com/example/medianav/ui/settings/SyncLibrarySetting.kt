package com.example.medianav.ui.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.medianav.ui.navigation.PluginViewModel
import com.example.medianav.ui.shared.LibraryQueryDialog
import com.example.medianav.ui.shared.NoPlugin
import com.example.medianav.ui.shared.Plugin
import com.example.plugin_common.plugin.ManualPlugin

@Composable
internal fun SyncLibrarySetting(
    settingsViewModel: SettingsViewModel,
    pluginViewModel: PluginViewModel
) {
    Setting(
        viewModel = settingsViewModel,
        title = "Sync Library",
        subtitle = "Sync library for a manual plugin",
        leftIcon = Icons.Outlined.Sync,
        type = SettingType.SYNC,
        dropdownContent = {
            SyncLibraryDropdown(settingsViewModel, pluginViewModel)
        }
    )
}

@Composable
fun SyncLibraryDropdown(
    settingsViewModel: SettingsViewModel,
    pluginViewModel: PluginViewModel
) {
    val manualEnabledPlugins by pluginViewModel.manualEnabledPlugins.collectAsState()

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
            items(manualEnabledPlugins) { plugin ->
                PluginItem(settingsViewModel, plugin)
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                )
            }

            if (manualEnabledPlugins.isEmpty()) {
                item {
                    NoPlugin(
                        "No manual plugins enabled",
                        "You have no plugins that can sync"
                    )
                }
            }
        }
    }
}

@Composable
fun PluginItem(
    viewModel: SettingsViewModel,
    plugin: ManualPlugin
) {
    val isSyncing by plugin.isSyncing.collectAsState(initial = false)

    var showQueryDialog by remember { mutableStateOf(false) }

    Plugin(
        plugin,
        content = {
            Button(
                onClick = { showQueryDialog = true },
                enabled = !isSyncing
            ) {
                Text(if (isSyncing) "Syncing..." else "Sync")
            }
        }
    )

    if (showQueryDialog) {
        LibraryQueryDialog(
            schema = plugin.querySchema,
            onDismiss = { showQueryDialog = false },
            onApply = { query ->
                showQueryDialog = false
                viewModel.syncPlugin(plugin, query)
            }
        )
    }
}
