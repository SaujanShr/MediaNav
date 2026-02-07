package com.example.medianav.library

import android.content.Context
import com.example.medianav.util.firstResult
import com.example.plugin_common.library.LibraryItem
import com.example.plugin_common.library.LibraryItemStatus
import com.example.plugin_common.plugin.MediaPlugin
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object LibraryManager {
    private val _libraryItems =
        MutableStateFlow<Map<String, Map<String, LibraryItem>>>(emptyMap())

    private val initMutex = Mutex()
    private var initialized = false
    suspend fun initialize(context: Context) {
        initMutex.withLock {
            if (initialized) return
            restoreItems(context)
            initialized = true
        }
    }

    suspend fun restoreItems(context: Context) {
        _libraryItems.value = LibraryStorage
            .libraryItemsMap(context)
            .firstResult()
            .getOrElse { emptyMap() }
    }

    fun addItemToCache(plugin: MediaPlugin, item: LibraryItem) {
        val existingItem = _libraryItems.value[plugin.info.id]?.get(item.id)
        val itemToAdd = existingItem?.let { item.copy(status = it.status) } ?: item
        setItemInCache(plugin, itemToAdd)
    }

    fun itemsForPlugin(plugin: MediaPlugin) = _libraryItems.map {
        it[plugin.info.id] ?: emptyMap()
    }

    suspend fun removeItemsForPlugin(context: Context, plugin: MediaPlugin) {
        LibraryStorage.removeItemsForPlugin(context, plugin.info.id)

        _libraryItems.update { current ->
            val newMap = current.toMutableMap()
            newMap.remove(plugin.info.id)
            newMap
        }
    }

    suspend fun setItemStatus(
        context: Context,
        plugin: MediaPlugin,
        item: LibraryItem,
        status: LibraryItemStatus
    ) {
        val updatedItem = item.copy(status = status)
        LibraryStorage.saveItem(context, plugin.info.id, updatedItem)
        setItemInCache(plugin, updatedItem)
    }

    private fun setItemInCache(plugin: MediaPlugin, item: LibraryItem) {
        _libraryItems.update { current ->
            val newMap = current.toMutableMap()
            val pluginMap = newMap.getOrPut(plugin.info.id) { emptyMap() }.toMutableMap()
            pluginMap[item.id] = item
            newMap[plugin.info.id] = pluginMap
            newMap
        }
    }
}
