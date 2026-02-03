package com.example.medianav.ui.settings

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medianav.config.ConfigManager
import com.example.medianav.ui.theme.Theme
import com.example.medianav.plugin.PluginManager
import com.example.plugin_common.library.LibraryQuery
import com.example.plugin_common.plugin.MediaPlugin
import com.example.plugin_common.plugin.ManualPlugin
import com.example.plugin_common.plugin.SecretPlugin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class SettingsViewModel : ViewModel() {
    private val _errors = MutableSharedFlow<String>()
    val errors = _errors.asSharedFlow()

    private val _expandedSetting = MutableStateFlow<SettingType?>(null)
    val expandedSetting: StateFlow<SettingType?> = _expandedSetting.asStateFlow()

    val theme = ConfigManager.theme

    fun toggleExpanded(setting: SettingType) {
        _expandedSetting.value = if (_expandedSetting.value == setting) null else setting
    }

    fun installPlugin(context: Context, uri: Uri?) {
        if (uri == null) {
            viewModelScope.launch { emitError("No file selected") }
            return
        }

        viewModelScope.launch {
            try {
                val file = copyUriToCache(context, uri)
                PluginManager.installPlugin(context, file.absolutePath)
                    .onFailure {
                        emitError(it.message ?: "Failed to install plugin")
                    }
            } catch (t: Throwable) {
                emitError(t.message ?: "Failed to install plugin")
            }
        }
    }

    fun uninstallPlugin(context: Context, plugin: MediaPlugin) {
        viewModelScope.launch {
            PluginManager.uninstallPlugin(context, plugin)
        }
    }

    fun syncPlugin(plugin: ManualPlugin, query: LibraryQuery) {
        viewModelScope.launch {
            if (!plugin.isSyncing.value) {
                plugin.sync(query)
            }
        }
    }

    fun setEnabled(context: Context, plugin: MediaPlugin, enabled: Boolean) {
        viewModelScope.launch {
            PluginManager.setEnabled(context, plugin, enabled)
        }
    }

    fun setTheme(context: Context, theme: Theme) {
        viewModelScope.launch {
            ConfigManager.setTheme(context, theme)
        }
    }

    fun secretsForPlugin(plugin: SecretPlugin): Map<String, String> =
        ConfigManager.secretsForPlugin(plugin)

    fun saveSecret(context: Context, plugin: SecretPlugin, key: String, value: String) {
        viewModelScope.launch {
            val currentSecrets = ConfigManager.secretsForPlugin(plugin).toMutableMap()
            currentSecrets[key] = value
            ConfigManager.setSecretsForPlugin(context, plugin, currentSecrets)
        }
    }

    private suspend fun emitError(message: String) {
        _errors.emit(message)
    }

    private suspend fun copyUriToCache(context: Context, uri: Uri): File =
        withContext(Dispatchers.IO) {
            val fileName = getFileName(context, uri)
            val destination = File(context.cacheDir, fileName)

            context.contentResolver.openInputStream(uri)
                ?.use { input -> destination.outputStream().use(input::copyTo) }
                ?: error("Cannot open selected file")

            destination
        }

    private fun getFileName(context: Context, uri: Uri): String =
        context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) cursor.getString(index) else "plugin.apk"
            } else {
                "plugin.apk"
            }
        } ?: "plugin.apk"
}
