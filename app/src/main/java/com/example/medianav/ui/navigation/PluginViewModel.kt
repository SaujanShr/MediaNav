package com.example.medianav.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medianav.plugin.PluginManager
import com.example.plugin_common.plugin.MediaPlugin
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

class PluginViewModel : ViewModel() {
    private val _currentPlugin = MutableStateFlow<MediaPlugin?>(null)
    val currentPlugin: StateFlow<MediaPlugin?> get() = _currentPlugin

    val plugins = PluginManager.plugins

    @OptIn(ExperimentalCoroutinesApi::class)
    val enabledPlugins = PluginManager.plugins
        .flatMapLatest { pluginList ->
            if (pluginList.isEmpty()) flowOf(emptyList())
            else combine(pluginList.map {
                PluginManager.isEnabled(it)
            }) { states ->
                pluginList.zip(states.toList()) { plugin, enabled ->
                    plugin.takeIf { enabled }
                }.filterNotNull()
            }
        }
        .stateIn(
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
}