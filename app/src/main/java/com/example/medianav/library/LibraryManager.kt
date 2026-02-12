package com.example.medianav.library

import android.content.Context
import com.example.plugin_common.util.firstResult
import com.example.plugin_common.library.LibraryItem
import com.example.plugin_common.library.LibraryItemStatus
import com.example.plugin_common.plugin.MediaPlugin
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object LibraryManager {
    private lateinit var appContext: Context
    private val mutex = Mutex()
    private var initialized = false
    suspend fun initialize(context: Context) = mutex.withLock {
        if (initialized) return
        appContext = context.applicationContext
        restoreItems()
        initialized = true
    }

    private val _libraryItems =
        MutableStateFlow<Map<String, Map<String, LibraryItem>>>(emptyMap())
    fun itemsForPlugin(plugin: MediaPlugin) = _libraryItems.map {
        it[plugin.metadata.id] ?: emptyMap()
    }

    suspend fun restoreItems() {
        _libraryItems.value = LibraryStorage
            .getLibraryItemsMap(appContext)
            .firstResult()
            .getOrElse { emptyMap() }
    }

    suspend fun addItem(plugin: MediaPlugin, item: LibraryItem) {
        LibraryStorage.addItemForPlugin(appContext, plugin, item)
        setItemInCache(plugin, item)
    }

    suspend fun removeItemsForPlugin(plugin: MediaPlugin) {
        LibraryStorage.removeItemsForPlugin(appContext, plugin)

        _libraryItems.update { current ->
            val newMap = current.toMutableMap()
            newMap.remove(plugin.metadata.id)
            newMap
        }
    }

    suspend fun setItemStatus(
        plugin: MediaPlugin,
        item: LibraryItem,
        status: LibraryItemStatus
    ) = mutex.withLock {
        val index = nextItemIndexForStatus(plugin, status)
        val updatedItem = item.copy(status = status, index = index)

        addItem(plugin, updatedItem)
    }

    suspend fun setItemIndex(
        plugin: MediaPlugin,
        item: LibraryItem,
        index: Int
    ) {
        val updatedItem = item.copy(index = index)

        LibraryStorage.addItemForPlugin(appContext, plugin, updatedItem)
        setItemInCache(plugin, updatedItem)
    }

    private fun nextItemIndexForStatus(plugin: MediaPlugin, status: LibraryItemStatus) =
        if (status == LibraryItemStatus.NONE) {
            0
        } else {
            val currentItems = _libraryItems.value[plugin.metadata.id] ?: emptyMap()
            val highestIndexForStatus = currentItems.values
                .filter { it.status == status }
                .maxOfOrNull { it.index } ?: -1

            highestIndexForStatus + 1
        }

    private fun setItemInCache(plugin: MediaPlugin, item: LibraryItem) =
        _libraryItems.update { current ->
            val updatedPluginMap = (current[plugin.metadata.id] ?: emptyMap()) + (item.id to item)
            current + (plugin.metadata.id to updatedPluginMap)
        }
}
