package com.example.plugin_anime.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Storage
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.plugin_common.plugin.PluginMetadata
import com.example.plugin_common.settings.SettingsManager
import com.example.plugin_common.settings.SettingsGroup
import com.example.plugin_common.settings.Setting

@Composable
internal fun AnimeSettingsScreen(
    metadata: PluginMetadata,
    genreCache: List<String>
) {
    val settingsGroups = listOf(
        createInfoGroup(metadata),
        createCacheGroup(genreCache.size)
    )

    val settingsManager = SettingsManager(settingsGroups)

    Column(modifier = Modifier.fillMaxSize()) {
        settingsManager.Content()
    }
}

private fun createInfoGroup(metadata: PluginMetadata): SettingsGroup {
    return SettingsGroup(
        "Plugin Information",
        listOf(
            Setting(
                title = metadata.name,
                subtitle = "Category: ${metadata.category.value}",
                leftIcon = Icons.Default.Info
            )
        )
    )
}

private fun createCacheGroup(genreCacheSize: Int): SettingsGroup {
    return SettingsGroup(
        "Cache",
        listOf(
            Setting(
                title = "Cache Status",
                subtitle = "$genreCacheSize genres cached",
                leftIcon = Icons.Default.Storage
            )
        )
    )
}


