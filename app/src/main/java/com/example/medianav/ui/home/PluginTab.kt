package com.example.medianav.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.medianav.ui.navigation.PluginViewModel
import com.example.medianav.ui.shared.NoPlugin
import com.example.medianav.ui.shared.Plugin
import com.example.plugin_common.util.toTitleCase
import com.example.plugin_common.plugin.MediaPlugin
import com.example.plugin_common.plugin.PluginCategory

private val tabs = listOf("All") + PluginCategory.entries.map {
    it.value.toTitleCase()
}

private fun getFilteredPlugins(plugins: List<MediaPlugin>, selectedIndex: Int): List<MediaPlugin> {
    if (selectedIndex == 0) return plugins
    val category = PluginCategory.entries.getOrNull(selectedIndex - 1)
        ?: return emptyList()

    return plugins.filter { it.metadata.category == category }
}

@Composable
internal fun PluginTab(
    pluginViewModel: PluginViewModel
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    val enabledPlugins by pluginViewModel.enabledPlugins.collectAsState()
    val filteredPlugins by remember(enabledPlugins, selectedTabIndex) {
        derivedStateOf { getFilteredPlugins(enabledPlugins, selectedTabIndex) }
    }

    Column {
        PluginTabRow(
            selectedIndex = selectedTabIndex,
            onTabSelected = { selectedTabIndex = it }
        )
        PluginTabContent(pluginViewModel, filteredPlugins)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PluginTabRow(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        tonalElevation = 6.dp
    ) {
        SecondaryScrollableTabRow(
            selectedTabIndex = selectedIndex,
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            indicator = {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(selectedIndex),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            tabs.forEachIndexed { index, tabName ->
                Tab(
                    selected = selectedIndex == index,
                    onClick = { onTabSelected(index) },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    text = {
                        Text(
                            text = tabName,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun PluginTabContent(
    pluginViewModel: PluginViewModel,
    plugins: List<MediaPlugin>
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(plugins) { plugin ->
            Plugin(plugin, onClick = {
                pluginViewModel.selectPlugin(plugin)
            })
        }

        if (plugins.isEmpty()) {
            item {
                NoPlugin(
                    "No plugins available",
                    "Add plugins to see them listed here"
                )
            }
        }
    }
}