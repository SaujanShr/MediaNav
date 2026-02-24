package com.example.plugin_anime.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.plugin_anime.domain.Anime
import com.example.plugin_anime.domain.Genre
import com.example.plugin_anime.domain.GenreQueryFilter
import com.example.plugin_common.plugin.PluginMetadata
import com.example.plugin_common.settings.SettingsContent
import com.example.plugin_common.settings.SettingsGroup
import com.example.plugin_common.settings.Setting

@Composable
internal fun AnimeSettingsScreen(
    metadata: PluginMetadata,
    animeCache: Map<Int, Anime>,
    genreCache: Map<GenreQueryFilter, List<Genre>>,
    onBack: () -> Unit
) {
    val settingsGroups = listOf(
        createInfoGroup(metadata),
        createCacheGroup(animeCache.size, genreCache.size)
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // Top bar with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Text(
                text = "Anime Plugin Settings",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Settings content
        SettingsContent(settingsGroups = settingsGroups)
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

private fun createCacheGroup(animeCacheSize: Int, genreCacheSize: Int): SettingsGroup {
    return SettingsGroup(
        "Cache",
        listOf(
            Setting(
                title = "Cache Status",
                subtitle = "$animeCacheSize anime cached • $genreCacheSize genres cached",
                leftIcon = Icons.Default.Storage
            )
        )
    )
}


