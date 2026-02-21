package com.example.medianav.plugin

import android.content.Context
import com.example.plugin_anime.AnimePlugin
import com.example.plugin_common.plugin.MediaPlugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File

object PluginManager {
    private lateinit var appContext: Context
    private val initMutex = Mutex()
    private var initialized = false
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    suspend fun initialize(context: Context) {
        initMutex.withLock {
            if (initialized) return
            appContext = context.applicationContext
            initialized = true
            restorePlugins()

            val animePlugin = AnimePlugin()
            val dataDir = PluginStorage.pluginDataDir(appContext, animePlugin.metadata.id)
            animePlugin.resources.attach(dataDir, appContext.resources)
            setPluginInCache(animePlugin)
            setEnabled(animePlugin, true)
        }
    }

    private val _plugins = MutableStateFlow<Map<String, MediaPlugin>>(emptyMap())
    val plugins: StateFlow<List<MediaPlugin>> = _plugins
        .map { it.values.toList() }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    private suspend fun restorePlugins() {
        PluginStorage.getPluginIds(appContext).first().forEach { pluginId ->
            runCatching {
                val apkFile = PluginStorage.getApkFile(appContext, pluginId)
                if (apkFile.exists()) {
                    val plugin = PluginLoader.loadPlugin(appContext, apkFile).getOrThrow()
                    setPluginInCache(plugin)
                }
            }
        }
    }

    suspend fun addPlugin(apkPath: String) = runCatching {
        val apkFile = loadFile(apkPath).getOrThrow()
        val plugin = PluginLoader.loadPlugin(appContext, apkFile).getOrThrow()
        
        plugin.initialize()
        PluginStorage.installPlugin(appContext, plugin, apkFile)
        setPluginInCache(plugin)
        plugin
    }

    suspend fun removePlugin(plugin: MediaPlugin) = runCatching {
        PluginStorage.uninstallPlugin(appContext, plugin)
        removePluginFromCache(plugin)
        plugin
    }

    val enabledPluginIds: Flow<Set<String>> get() =
        PluginStorage.getEnabledPluginIds(appContext)

    suspend fun setEnabled(plugin: MediaPlugin, enabled: Boolean) {
        PluginStorage.setEnabled(appContext, plugin, enabled)
    }

    private fun loadFile(path: String) = runCatching {
        val file = File(path)
        require(file.exists()) { "File $path does not exist" }
        require(file.isFile) { "Path $path exists but is not a file" }
        file
    }

    private fun setPluginInCache(plugin: MediaPlugin) = _plugins.update { current ->
        current + (plugin.metadata.id to plugin)
    }

    private fun removePluginFromCache(plugin: MediaPlugin) = _plugins.update { current ->
        current - plugin.metadata.id
    }
}