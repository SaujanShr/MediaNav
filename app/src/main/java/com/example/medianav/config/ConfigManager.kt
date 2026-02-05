package com.example.medianav.config

import android.content.Context
import com.example.medianav.ui.theme.Theme
import com.example.medianav.util.firstResult
import com.example.plugin_common.plugin.SecretPlugin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object ConfigManager {
    private val _theme = MutableStateFlow(Theme.DEFAULT)
    val theme = _theme.asStateFlow()

    private val _secrets = MutableStateFlow<Map<PluginId, Secrets>>(emptyMap())

    private val initMutex = Mutex()
    private var initialized = false
    suspend fun initialize(context: Context) = initMutex.withLock {
        if (initialized) return
        restoreTheme(context)
        restoreSecrets(context)
        initialized = true
    }

    private suspend fun restoreTheme(context: Context) {
        _theme.value = ConfigStorage
            .theme(context)
            .firstResult()
            .getOrElse { Theme.DEFAULT }
    }

    suspend fun setTheme(context: Context, newTheme: Theme) {
        ConfigStorage.setTheme(context, newTheme)
        _theme.value = newTheme
    }

    private suspend fun restoreSecrets(context: Context) {
        _secrets.value = ConfigStorage
            .secretsMap(context)
            .firstResult()
            .getOrElse { emptyMap() }
    }

    fun secretsForPlugin(plugin: SecretPlugin): Secrets {
        val secrets = _secrets.value[plugin.info.id]
        return secrets ?: emptyMap()
    }

    suspend fun setSecretsForPlugin(
        context: Context,
        plugin: SecretPlugin,
        secrets: Map<String, String>
    ) {
        ConfigStorage.setSecretsForPlugin(context, plugin.info.id, secrets)
        _secrets.update { current -> current + (plugin.info.id to secrets) }
    }

    suspend fun removeSecretsForPlugin(context: Context, plugin: SecretPlugin) {
        ConfigStorage.removeSecretsForPlugin(context, plugin.info.id)
        _secrets.update { current -> current - plugin.info.id }
    }
}
