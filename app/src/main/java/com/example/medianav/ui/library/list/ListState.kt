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
    val queryScrollIndex: StateFlow<Int>
    val queryScrollOffset: StateFlow<Int>

    fun setPlugin(plugin: MediaPlugin?)
    fun setMode(mode: LibraryMode)
    fun setPage(page: Int)
    fun setQueryScrollPosition(index: Int, offset: Int)
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

    private val _queryScrollIndex = MutableStateFlow(0)
    override val queryScrollIndex = _queryScrollIndex.asStateFlow()

    private val _queryScrollOffset = MutableStateFlow(0)
    override val queryScrollOffset = _queryScrollOffset.asStateFlow()

    override fun setQueryScrollPosition(index: Int, offset: Int) {
        _queryScrollIndex.value = index
        _queryScrollOffset.value = offset
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

    override val queryItems: Flow<PagingData<LibraryItem>> by lazy {
        combine(_currentPlugin, _mode, ::Pair)
            .flatMapLatest { (plugin, mode) -> getQueryItemsForMode(plugin, mode) }
            .cachedIn(scope)
    }

    private fun filterNewOnly(pagingData: PagingData<LibraryItem>): PagingData<LibraryItem> {
        return pagingData.filter { it.status == LibraryItemStatus.NONE }
    }

    private fun getQueryModeItems(
        plugin: MediaPlugin?,
        mode: LibraryMode.Query
    ): Flow<PagingData<LibraryItem>> {
        if (plugin == null) return flowOf(PagingData.empty())

        return plugin.getLibraryItems(LibraryQuery())
            .map { pagingData ->
                val items = itemMap.value
                pagingData
                    .map { remoteItem ->
                        items[remoteItem.id]
                            ?.copy(index = remoteItem.index)
                            ?: remoteItem
                    }.let {
                        if (mode.type == QueryModeType.NEW_ONLY) filterNewOnly(it)
                        else it
                    }
            }
    }

    private fun getQueryItemsForMode(
        plugin: MediaPlugin?,
        mode: LibraryMode
    ): Flow<PagingData<LibraryItem>> {
        return when (mode) {
            is LibraryMode.Query -> getQueryModeItems(plugin, mode)
            is LibraryMode.List -> flowOf(PagingData.empty())
        }
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
        combine(itemMap, _mode, _currentPage) { items, mode, page ->
            val items = listItemsForMode(items, mode)
            val sortedItems = sortForMode(items, mode)

            val pagedItems = sortedItems
                .drop(page * LibraryConstants.PAGE_SIZE)
                .take(LibraryConstants.PAGE_SIZE)

            PagingData.from(pagedItems)
        }.cachedIn(scope)
    }
}