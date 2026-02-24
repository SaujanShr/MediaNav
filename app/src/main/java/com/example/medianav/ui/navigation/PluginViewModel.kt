package com.example.medianav.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medianav.plugin.PluginManager
import com.example.plugin_common.plugin.MediaPlugin
import kotlinx.coroutines.flow.*

class PluginViewModel : ViewModel() {
    private val _currentPlugin = MutableStateFlow<MediaPlugin?>(null)
    val currentPlugin: StateFlow<MediaPlugin?> get() = _currentPlugin

    private val _pluginSettings = MutableStateFlow<MediaPlugin?>(null)
    val pluginSettings: StateFlow<MediaPlugin?> get() = _pluginSettings

    val plugins = PluginManager.plugins

    val enabledPlugins: StateFlow<List<MediaPlugin>> = combine(
        PluginManager.plugins,
        PluginManager.enabledPluginIds
    ) { pluginList, enabledIds ->
        pluginList.filter { plugin ->
            enabledIds.contains(plugin.metadata.id)
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    fun selectPlugin(plugin: MediaPlugin) {
        _currentPlugin.value = plugin
    }

    fun clearPlugin() {
        _currentPlugin.value = null
    }

    fun openPluginSettings(plugin: MediaPlugin) {
        _pluginSettings.value = plugin
    }

    fun closePluginSettings() {
        _pluginSettings.value = null
    }
}