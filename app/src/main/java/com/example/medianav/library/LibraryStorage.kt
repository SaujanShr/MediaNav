package com.example.medianav.library

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.plugin_common.library.LibraryItem
import com.example.plugin_common.plugin.MediaPlugin
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

internal object LibraryStorage {
    private val Context.libraryDataStore by preferencesDataStore(LibraryConstants.DATA_STORE_NAME)
    private val itemsKey = stringPreferencesKey(LibraryConstants.Keys.ITEMS)

    private val gson = Gson()
    private val nestedMapType =
        object : TypeToken<MutableMap<String, MutableMap<String, LibraryItem>>>() {}.type

    fun getLibraryItemsMap(context: Context) =
        context.libraryDataStore.data
            .catch { e ->
                if (e is IOException) emit(emptyPreferences())
                else throw e
            }
            .map { prefs -> getLibraryItemsMap(prefs) }

    suspend fun addItemForPlugin(context: Context, plugin: MediaPlugin, item: LibraryItem) =
        context.libraryDataStore.edit { prefs ->
            val map = getLibraryItemsMap(prefs)
            val pluginMap = map.getOrPut(plugin.metadata.id) { mutableMapOf() }
            pluginMap[item.id] = item
            prefs[itemsKey] = gson.toJson(map)
        }

    suspend fun removeItemsForPlugin(context: Context, plugin: MediaPlugin) =
        context.libraryDataStore.edit { prefs ->
            val map = getLibraryItemsMap(prefs)
            val removed = map.remove(plugin.metadata.id) != null
            if (removed) prefs[itemsKey] = gson.toJson(map)
        }

    private fun getLibraryItemsMap(prefs: Preferences):
            MutableMap<String, MutableMap<String, LibraryItem>> {
        val json = prefs[itemsKey] ?: "{}"
        return gson.fromJson(json, nestedMapType)
    }
}