package com.example.medianav.plugin

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences.Key
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File
import java.io.IOException

internal object PluginStorage {
    private val Context.pluginsDataStore by preferencesDataStore(PluginConstants.DATA_STORE_NAME)
    private val installedKey = stringSetPreferencesKey(PluginConstants.Keys.INSTALLED_IDS)
    private val enabledKey = stringSetPreferencesKey(PluginConstants.Keys.ENABLED_IDS)

    private fun pluginsApkDir(context: Context): File =
        File(context.filesDir, PluginConstants.Paths.APK_DIR).ensureExists()

    private fun pluginsDataDir(context: Context): File =
        File(context.filesDir, PluginConstants.Paths.DATA_DIR).ensureExists()

    private fun File.ensureExists(): File {
        if (!exists() && !mkdirs()) {
            throw IOException("Failed to create directory: $absolutePath")
        }
        return this
    }

    private fun pluginApkFile(context: Context, pluginId: PluginId) =
        File(pluginsApkDir(context), "$pluginId.apk")

    fun pluginDataDir(context: Context, pluginId: PluginId): File =
        File(pluginsDataDir(context), pluginId).ensureExists()

    fun deletePluginDataDir(context: Context, pluginId: PluginId) {
        val dir = pluginDataDir(context, pluginId)
        if (dir.exists() && !dir.deleteRecursively()) {
            throw IOException(
                "Failed to delete plugin data directory: ${dir.absolutePath}"
            )
        }
    }

    private fun installedPluginIds(context: Context): Flow<Set<PluginId>> =
        context.pluginsDataStore.data
            .catch { e ->
                if (e is IOException) emit(emptyPreferences())
                else throw e
            }
            .map { prefs -> prefs[installedKey] ?: emptySet() }

    fun enabledPluginIds(context: Context): Flow<Set<PluginId>> =
        context.pluginsDataStore.data
            .catch { e ->
                if (e is IOException) emit(emptyPreferences())
                else throw e
            }
            .map { prefs -> prefs[enabledKey] ?: emptySet() }

    fun loadFile(path: String): Result<File> = runCatching {
        val file = File(path)
        require(file.exists()) { "File $path does not exist" }
        require(file.isFile) { "Path $path exists but is not a file" }
        file
    }

    suspend fun savePluginFile(context: Context, pluginId: PluginId, sourceApk: File) {
        val file = pluginApkFile(context, pluginId)
        sourceApk.copyTo(file, overwrite = true)

        addInstalledPluginId(context, pluginId)
        setEnabledPluginId(context, pluginId, true)
    }

    suspend fun deletePluginFile(context: Context, pluginId: PluginId) {
        pluginApkFile(context, pluginId).delete()
        removeInstalledPluginId(context, pluginId)
        setEnabledPluginId(context, pluginId, false)
    }

    suspend fun loadAllPluginFiles(context: Context): List<File> {
        val ids = installedPluginIds(context).first()
        return ids.mapNotNull { pluginId ->
            val apkFile = pluginApkFile(context, pluginId)
            loadFile(apkFile.path).fold(
                onSuccess = { it },
                onFailure = {
                    removeInstalledPluginId(context, pluginId)
                    null
                }
            )
        }
    }

    private suspend fun addInstalledPluginId(context: Context, pluginId: PluginId) =
        context.pluginsDataStore.edit { it.addToSet(installedKey, pluginId) }

    private suspend fun removeInstalledPluginId(context: Context, pluginId: PluginId) =
        context.pluginsDataStore.edit { it.removeFromSet(installedKey, pluginId) }

    suspend fun setEnabledPluginId(context: Context, pluginId: PluginId, enabled: Boolean) =
        context.pluginsDataStore.edit {
            if (enabled) it.addToSet(enabledKey, pluginId)
            else it.removeFromSet(enabledKey, pluginId)
        }

    private fun MutablePreferences.addToSet(key: Key<Set<PluginId>>, value: PluginId) {
        val existing = this[key] ?: emptySet()
        this[key] = existing + value
    }

    private fun MutablePreferences.removeFromSet(key: Key<Set<PluginId>>, value: PluginId) {
        val existing = this[key] ?: emptySet()
        this[key] = existing - value
    }
}