package com.example.medianav.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.medianav.ui.navigation.PluginViewModel
import com.example.medianav.ui.shared.NoPlugin
import com.example.plugin_common.plugin.MediaPlugin
import com.example.plugin_common.plugin.SecretPlugin

@Composable
internal fun PluginSecretsSetting(
    settingsViewModel: SettingsViewModel,
    pluginViewModel: PluginViewModel
) {
    Setting(
        viewModel = settingsViewModel,
        title = "Plugin Secrets",
        subtitle = "Manage API keys and secrets",
        leftIcon = Icons.Outlined.Key,
        type = SettingType.SECRETS,
        dropdownContent = {
            PluginSecretsDropdown(settingsViewModel, pluginViewModel)
        }
    )
}

@Composable
private fun PluginSecretsDropdown(
    settingsViewModel: SettingsViewModel,
    pluginViewModel: PluginViewModel
) {
    val secretPlugins by pluginViewModel.secretEnabledPlugins.collectAsState()

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
        tonalElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 400.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(secretPlugins) { plugin ->
                SecretPluginItem(settingsViewModel, plugin)
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                )
            }

            if (secretPlugins.isEmpty()) {
                item {
                    NoPlugin(
                        "No secret plugins enabled",
                        "You have no plugins that need secrets"
                    )
                }
            }
        }
    }
}

@Composable
private fun SecretPluginItem(settingsViewModel: SettingsViewModel, plugin: SecretPlugin) {
    val context = LocalContext.current
    val pluginSecrets = settingsViewModel.secretsForPlugin(plugin)

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = plugin.info.name,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        plugin.secretKeys.forEach { key ->
            var value by remember(key, pluginSecrets) { 
                mutableStateOf(pluginSecrets[key] ?: "") 
            }

            OutlinedTextField(
                value = value,
                onValueChange = { 
                    value = it
                    settingsViewModel.saveSecret(context, plugin, key, it)
                },
                label = { Text(key) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                singleLine = true
            )
        }
    }
}
