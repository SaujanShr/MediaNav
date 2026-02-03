package com.example.medianav.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medianav.config.ConfigManager
import com.example.medianav.plugin.PluginManager
import com.example.plugin_common.plugin.ManualPlugin
import com.example.plugin_common.plugin.MediaPlugin
import com.example.plugin_common.plugin.SecretPlugin
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PluginViewModel : ViewModel() {
    private val _errors = MutableSharedFlow<String>()
    val errors = _errors.asSharedFlow()

    private val _currentPlugin = MutableStateFlow<MediaPlugin?>(null)
    val currentPlugin: StateFlow<MediaPlugin?> get() = _currentPlugin

    val plugins = PluginManager.plugins

    @OptIn(ExperimentalCoroutinesApi::class)
    val enabledPlugins = PluginManager.plugins
        .flatMapLatest { pluginList ->
            if (pluginList.isEmpty()) flowOf(emptyList())
            else combine(pluginList.map { PluginManager.isEnabled(it) }) { states ->
                pluginList.zip(states.toList()) { plugin, enabled -> plugin.takeIf { enabled } }
                    .filterNotNull()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val manualEnabledPlugins = enabledPlugins
        .map { it.filterIsInstance<ManualPlugin>() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val secretEnabledPlugins = enabledPlugins
        .map { it.filterIsInstance<SecretPlugin>() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        _currentPlugin
            .filterIsInstance<SecretPlugin>()
            .onEach { plugin ->
                try {
                    val secrets = ConfigManager.secretsForPlugin(plugin)
                    plugin.setSecrets(secrets)
                } catch (e: Exception) {
                    viewModelScope.launch {
                        _errors.emit(e.message ?: "Failed to set secrets")
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun selectPlugin(plugin: MediaPlugin) {
        _currentPlugin.value = plugin
    }

    fun clearPlugin() {
        _currentPlugin.value = null
    }
}