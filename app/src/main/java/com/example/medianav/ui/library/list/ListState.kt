package com.example.medianav.ui.library.list

import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.map
import com.example.medianav.library.LibraryConstants
import com.example.medianav.library.LibraryManager
import com.example.medianav.ui.library.mode.LibraryMode
import com.example.medianav.ui.library.mode.ListModeSort
import com.example.medianav.ui.library.mode.ListModeStatus
import com.example.medianav.ui.library.mode.QueryModeType
import com.example.plugin_common.library.LibraryItem
import com.example.plugin_common.library.LibraryItemStatus
import com.example.plugin_common.library.LibraryQuery
import com.example.plugin_common.plugin.MediaPlugin
import com.example.plugin_common.util.conditionally
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

interface ListStateProvider {
    val currentPlugin: StateFlow<MediaPlugin?>
    val mode: StateFlow<LibraryMode>
    val currentPage: StateFlow<Int>
    val totalPages: StateFlow<Int>
    val queryItems: Flow<PagingData<LibraryItem>>
    val listItems: Flow<PagingData<LibraryItem>>

    fun setPlugin(plugin: MediaPlugin?)
    fun setMode(mode: LibraryMode)
    fun setPage(page: Int)
    fun toggleEditMode()
    suspend fun moveItem(item: LibraryItem, fromIndex: Int, toIndex: Int)
}

class ListState: ListStateProvider {
    private lateinit var scope: CoroutineScope

    fun initialize(scope: CoroutineScope) {
        this.scope = scope
    }

    private val _currentPlugin = MutableStateFlow<MediaPlugin?>(null)
    override val currentPlugin = _currentPlugin.asStateFlow()

    override fun setPlugin(plugin: MediaPlugin?) {
        _currentPlugin.value = plugin
    }

    private val _mode = MutableStateFlow<LibraryMode>(
        LibraryMode.Query(QueryModeType.ALL)
    )
    override val mode = _mode.asStateFlow()

    override fun setMode(mode: LibraryMode) {
        _mode.value = mode
    }

    private val _currentPage = MutableStateFlow(0)
    override val currentPage = _currentPage.asStateFlow()

    override fun setPage(page: Int) {
        _currentPage.value = page
    }


    private val _totalPages: StateFlow<Int> by lazy {
        combine(itemMap, _mode) { items, mode ->
            when (mode) {
                is LibraryMode.Query -> 0
                is LibraryMode.List -> {
                    val filteredItems = listItemsForMode(items, mode)
                    (filteredItems.size + LibraryConstants.PAGE_SIZE - 1) / LibraryConstants.PAGE_SIZE
                }
            }
        }.stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )
    }
    override val totalPages: StateFlow<Int> by lazy { _totalPages }

    private val itemMap by lazy {
        _currentPlugin.flatMapLatest { plugin ->
            if (plugin == null) flowOf(emptyMap())
            else LibraryManager.itemsForPlugin(plugin)
        }.stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyMap()
        )
    }

    private fun queryItemsForMode(
        plugin: MediaPlugin?,
        mode: LibraryMode,
        items: Map<String, LibraryItem>
    ): Flow<PagingData<LibraryItem>> {
        return when (mode) {
            is LibraryMode.List -> flowOf(PagingData.empty())
            is LibraryMode.Query -> {
                plugin?.getLibraryItems(LibraryQuery())?.map { pagingData ->
                    pagingData
                        .map { remoteItem ->
                            items[remoteItem.id]
                                ?.copy(index = remoteItem.index)
                                ?: remoteItem
                        }.conditionally(mode.type == QueryModeType.NEW_ONLY) {
                            it.filter { item -> item.status == LibraryItemStatus.NONE }
                        }
                } ?: flowOf(PagingData.empty())
            }
        }
    }

    override val queryItems: Flow<PagingData<LibraryItem>> by lazy {
        combine(_currentPlugin, _mode, itemMap, ::Triple)
            .flatMapLatest { (plugin, mode, items) ->
                queryItemsForMode(plugin, mode, items)
            }.cachedIn(scope)
    }

    private fun listItemsForMode(
        items: Map<String, LibraryItem>,
        mode: LibraryMode
    ): List<LibraryItem> = when (mode) {
        is LibraryMode.Query -> emptyList()
        is LibraryMode.List -> {
            when (mode.status) {
                ListModeStatus.VIEWED -> items.values.filter { it.status == LibraryItemStatus.VIEWED }
                ListModeStatus.LIKED -> items.values.filter { it.status == LibraryItemStatus.LIKED }
                ListModeStatus.SAVED -> items.values.filter { it.saved }
            }
        }
    }

    private fun sortForMode(
        items: List<LibraryItem>,
        mode: LibraryMode
    ) = when (mode) {
        is LibraryMode.Query -> items.sortedBy { it.index }
        is LibraryMode.List -> {
            if (mode.sort == ListModeSort.BY_ACCESS) {
                items.sortedByDescending { it.lastAccessed }
            } else {
                items.sortedBy { it.index }
            }
        }
    }

    override val listItems by lazy {
        combine(itemMap, _mode) { items, mode ->
            val filteredItems = listItemsForMode(items, mode)
            val sortedItems = sortForMode(filteredItems, mode)

            PagingData.from(sortedItems)
        }.cachedIn(scope)
    }

    override fun toggleEditMode() {
        val current = _mode.value
        if (current is LibraryMode.List) {
            _mode.value = current.copy(isEdit = !current.isEdit)
        }
    }

    override suspend fun moveItem(item: LibraryItem, fromIndex: Int, toIndex: Int) {
        val plugin = _currentPlugin.value ?: return
        val items = itemMap.value
        val mode = _mode.value

        if (mode !is LibraryMode.List) return

        val allItems = listItemsForMode(items, mode)
            .sortedBy { it.index }
            .toMutableList()

        val itemToMove = allItems.find { it.id == item.id } ?: return
        val oldPosition = allItems.indexOf(itemToMove)

        if (oldPosition == -1 || oldPosition == toIndex) return

        allItems.removeAt(oldPosition)
        allItems.add(toIndex, itemToMove)

        allItems.forEachIndexed { index, libraryItem ->
            if (libraryItem.index != index) {
                LibraryManager.setIndex(plugin, libraryItem, index)
            }
        }
    }
}
