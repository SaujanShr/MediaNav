package com.example.medianav.ui.settings

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medianav.config.ConfigManager
import com.example.medianav.ui.theme.Theme
import com.example.medianav.plugin.PluginManager
import com.example.plugin_common.plugin.MediaPlugin
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
    val plugins = PluginManager.plugins

    fun toggleExpanded(setting: SettingType) {
        _expandedSetting.value = if (_expandedSetting.value == setting) null else setting
    }

    fun isPluginEnabled(plugin: MediaPlugin): Flow<Boolean> {
        return PluginManager.isEnabled(plugin)
    }

    fun installPlugin(context: Context, uri: Uri?) {
        if (uri == null) {
            viewModelScope.launch { emitError("No file selected") }
            return
        }

        viewModelScope.launch {
            try {
                val file = copyUriToCache(context, uri)
                PluginManager.addPlugin(file.absolutePath)
                    .onFailure {
                        emitError(it.message ?: "Failed to install plugin")
                    }
            } catch (t: Throwable) {
                emitError(t.message ?: "Failed to install plugin")
            }
        }
    }

    fun uninstallPlugin(plugin: MediaPlugin) {
        viewModelScope.launch {
            PluginManager.removePlugin(plugin)
        }
    }

    fun setEnabled(plugin: MediaPlugin, enabled: Boolean) {
        viewModelScope.launch {
            PluginManager.setEnabled(plugin, enabled)
        }
    }

    fun setTheme(theme: Theme) {
        viewModelScope.launch {
            ConfigManager.setTheme(theme)
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