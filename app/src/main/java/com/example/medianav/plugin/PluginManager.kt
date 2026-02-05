package com.example.medianav.plugin

import android.content.Context
import com.example.plugin_anime.AnimePlugin
import com.example.plugin_common.plugin.MediaPlugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object PluginManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _plugins = MutableStateFlow<List<MediaPlugin>>(emptyList())
    val plugins: StateFlow<List<MediaPlugin>> = _plugins.asStateFlow()

    private val _enabledPluginIds = MutableStateFlow<Set<String>>(emptySet())

    private val initMutex = Mutex()
    private var initialized = false
    suspend fun initialize(context: Context) {
        initMutex.withLock {
            if (initialized) return
            initialized = true
            restorePlugins(context)
            restoreEnabledPluginIds(context)

            val plugin = AnimePlugin()
            setPluginInCache(plugin)
            setEnabled(context, plugin, true)
        }
    }

    private suspend fun restorePlugins(context: Context) {
        val pluginFiles = PluginStorage.loadAllPluginFiles(context)
        val loadedPlugins = pluginFiles.mapNotNull { file ->
            PluginLoader.loadPlugin(context, file).getOrNull()
        }
        _plugins.value = loadedPlugins.sortedBy { it.info.name.lowercase() }
    }

    private suspend fun restoreEnabledPluginIds(context: Context) {
        _enabledPluginIds.value = PluginStorage.enabledPluginIds(context).first()
    }

    suspend fun installPlugin(context: Context, apkPath: String): Result<MediaPlugin> {
        val apkFile = PluginStorage.loadFile(apkPath)
            .getOrElse { return Result.failure(it) }

        val result = PluginLoader.loadPlugin(context, apkFile)
        result.onSuccess { plugin ->
            PluginStorage.savePluginFile(context, plugin.info.id, apkFile)
            setPluginInCache(plugin)
            setEnabled(context, plugin, true)
        }

        return result
    }

    suspend fun uninstallPlugin(context: Context, plugin: MediaPlugin) {
        PluginStorage.deletePluginFile(context, plugin.info.id)
        PluginStorage.deletePluginDataDir(context, plugin.info.id)
        removePlugin(plugin)
        setEnabled(context, plugin, false)
    }

    fun isEnabled(plugin: MediaPlugin): StateFlow<Boolean> =
        _enabledPluginIds
            .map { plugin.info.id in it }
            .distinctUntilChanged()
            .stateIn(
                scope,
                SharingStarted.WhileSubscribed(5_000),
                false
            )

    suspend fun setEnabled(context: Context, plugin: MediaPlugin, enabled: Boolean) {
        PluginStorage.setEnabledPluginId(context, plugin.info.id, enabled)
        _enabledPluginIds.update { current ->
            if (enabled) current + plugin.info.id
            else current - plugin.info.id
        }
    }

    private fun removePlugin(plugin: MediaPlugin) {
        _plugins.update { current -> current.filter { it.info.id != plugin.info.id } }
    }

    private fun setPluginInCache(plugin: MediaPlugin) =
        _plugins.update { current ->
            current.toMutableList().apply {
                val index = indexOfFirst { it.info.id == plugin.info.id }
                if (index >= 0) this[index] = plugin else add(plugin)
            }
            .sortedBy {
                it.info.name.lowercase()
            }
        }
}