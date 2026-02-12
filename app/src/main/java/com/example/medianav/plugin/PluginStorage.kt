package com.example.medianav.plugin

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.plugin_common.plugin.MediaPlugin
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import com.google.gson.Gson
import java.io.File
import java.io.IOException

internal object PluginStorage {
    private val Context.pluginsDataStore by preferencesDataStore(PluginConstants.DATA_STORE_NAME)
    private val installedPluginsKey =
        stringSetPreferencesKey(PluginConstants.Keys.INSTALLED_PLUGINS)
    private val enabledPluginsKey =
        stringSetPreferencesKey(PluginConstants.Keys.ENABLED_PLUGINS)
    private val secretsKey = stringPreferencesKey(PluginConstants.Keys.SECRETS)

    private val gson = Gson()
    private val secretsMapType = object : TypeToken<Map<String, Map<String, String>>>() {}.type

    fun getPluginIds(context: Context) =
        context.pluginsDataStore.data
            .catch { e ->
                if (e is IOException) emit(emptyPreferences())
                else throw e
            }
            .map { prefs -> prefs[installedPluginsKey] ?: emptySet() }

    suspend fun installPlugin(context: Context, plugin: MediaPlugin, apkFile: File) {
        val pluginId = plugin.metadata.id

        saveApkInternally(context, pluginId, apkFile)
        pluginDataDir(context, pluginId).ensureExists()

        context.pluginsDataStore.edit { prefs ->
            val installedIds = (prefs[installedPluginsKey] ?: emptySet()).toMutableSet()
            installedIds.add(pluginId)
            prefs[installedPluginsKey] = installedIds

            val enabledIds = (prefs[enabledPluginsKey] ?: emptySet()).toMutableSet()
            enabledIds.add(pluginId)
            prefs[enabledPluginsKey] = enabledIds
        }
    }

    suspend fun uninstallPlugin(context: Context, plugin: MediaPlugin) {
        val pluginId = plugin.metadata.id

        removeApkInternally(context, pluginId)
        pluginDataDir(context, pluginId).deleteSafely()

        context.pluginsDataStore.edit { prefs ->
            val installedIds = (prefs[installedPluginsKey] ?: emptySet()).toMutableSet()
            installedIds.remove(pluginId)
            prefs[installedPluginsKey] = installedIds

            val enabledIds = (prefs[enabledPluginsKey] ?: emptySet()).toMutableSet()
            enabledIds.remove(pluginId)
            prefs[enabledPluginsKey] = enabledIds

            val secrets = getSecretsMap(prefs).toMutableMap()
            secrets.remove(pluginId)
            prefs[secretsKey] = gson.toJson(secrets)
        }
    }

    fun getEnabledPluginIds(context: Context): Flow<Set<String>> =
        context.pluginsDataStore.data
            .catch { e ->
                if (e is IOException) emit(emptyPreferences())
                else throw e
            }
            .map { prefs -> prefs[enabledPluginsKey] ?: emptySet() }


    suspend fun setEnabled(context: Context, plugin: MediaPlugin, enabled: Boolean) {
        context.pluginsDataStore.edit { prefs ->
            val enabledIds = (prefs[enabledPluginsKey] ?: emptySet()).toMutableSet()
            if (enabled) {
                enabledIds.add(plugin.metadata.id)
            } else {
                enabledIds.remove(plugin.metadata.id)
            }
            prefs[enabledPluginsKey] = enabledIds
        }
    }

    fun getSecrets(context: Context): Flow<Map<String, Map<String, String>>> =
        context.pluginsDataStore.data
            .catch { e ->
                if (e is IOException) emit(emptyPreferences())
                else throw e
            }
            .map { prefs -> getSecretsMap(prefs) }

    suspend fun addSecrets(context: Context, pluginId: String, secrets: Map<String, String>) {
        context.pluginsDataStore.edit { prefs ->
            val allSecrets = getSecretsMap(prefs).toMutableMap()
            allSecrets[pluginId] = secrets
            prefs[secretsKey] = gson.toJson(allSecrets)
        }
    }

    private fun saveApkInternally(context: Context, pluginId: String, apkFile: File): File {
        val apkDir = File(context.noBackupFilesDir, PluginConstants.Paths.APK_DIR)
            .ensureExists()
        val destination = File(apkDir, "${pluginId}.apk")
        apkFile.copyTo(destination, overwrite = true)
        return destination
    }

    private fun removeApkInternally(context: Context, pluginId: String) {
        val apkFile = getApkFile(context, pluginId)
        if (apkFile.exists()) apkFile.delete()
    }

    fun getApkFile(context: Context, pluginId: String): File {
        val apkDir = File(context.noBackupFilesDir, PluginConstants.Paths.APK_DIR)
        return File(apkDir, "${pluginId}.apk")
    }

    fun pluginDataDir(context: Context, pluginId: String) =
        File(pluginsDataDir(context), pluginId)

    private fun pluginsDataDir(context: Context) =
        File(context.noBackupFilesDir, PluginConstants.Paths.DATA_DIR)

    private fun File.ensureExists(): File {
        if (!exists() && !mkdirs()) {
            throw IOException("Failed to create directory: $absolutePath")
        }
        return this
    }

    private fun File.deleteSafely(): File {
        if (exists() && !deleteRecursively()) {
            throw IOException("Failed to delete directory: $absolutePath")
        }
        return this
    }

    private fun getSecretsMap(prefs: Preferences): Map<String, Map<String, String>> {
        val json = prefs[secretsKey] ?: "{}"
        return gson.fromJson(json, secretsMapType) ?: emptyMap()
    }
}