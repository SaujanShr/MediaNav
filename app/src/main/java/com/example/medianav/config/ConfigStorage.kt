package com.example.medianav.config

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.medianav.ui.theme.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

internal object ConfigStorage {
    private val Context.configDataStore by preferencesDataStore(ConfigConstants.DATA_STORE_NAME)
    private val themeKey = stringPreferencesKey(ConfigConstants.Keys.THEME)


    fun getTheme(context: Context): Flow<Theme> =
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
}