package com.example.medianav.config

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.medianav.ui.theme.Theme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

internal object ConfigStorage {
    private val Context.configDataStore by preferencesDataStore(ConfigConstants.DATA_STORE_NAME)
    private val themeKey = stringPreferencesKey(ConfigConstants.Keys.THEME)
    private val secretsKey = stringPreferencesKey(ConfigConstants.Keys.SECRETS)

    private val gson = Gson()
    private val nestedMapType = object : TypeToken<MutableMap<String, Map<String, String>>>() {}.type


    fun theme(context: Context): Flow<Theme> =
        context.configDataStore.data
            .catch { e ->
                if (e is IOException) emit(emptyPreferences())
                else throw e
            }
            .map { prefs -> Theme.fromValue(prefs[themeKey]) }

    suspend fun setTheme(context: Context, theme: Theme) =
        context.configDataStore.edit { prefs ->
            prefs[themeKey] = theme.value
        }

    fun secretsMap(context: Context): Flow<Map<String, Map<String, String>>> =
        context.configDataStore.data
            .catch { e ->
                if (e is IOException) emit(emptyPreferences())
                else throw e
            }
            .map { prefs -> getSecretsMap(prefs) }

    suspend fun setSecretsForPlugin(context: Context, pluginId: String, secrets: Map<String, String>) =
        context.configDataStore.edit { prefs ->
            val map = getSecretsMap(prefs)
            map[pluginId] = secrets.toMutableMap()
            prefs[secretsKey] = gson.toJson(map)
        }

    suspend fun removeSecretsForPlugin(context: Context, pluginId: String) =
        context.configDataStore.edit { prefs ->
            val map = getSecretsMap(prefs)
            val removed = map.remove(pluginId) != null
            if (removed) prefs[secretsKey] = gson.toJson(map)
        }

    private fun getSecretsMap(prefs: Preferences): MutableMap<String, Map<String, String>> {
        val json = prefs[secretsKey] ?: "{}"
        return gson.fromJson(json, nestedMapType)
    }
}