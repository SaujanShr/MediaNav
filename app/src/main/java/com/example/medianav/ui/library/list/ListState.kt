package com.example.medianav.ui.library.list

import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.map
import com.example.medianav.library.LibraryConstants
import com.example.medianav.library.LibraryManager
import com.example.plugin_common.library.LibraryItem
import com.example.plugin_common.library.LibraryItemStatus
import com.example.plugin_common.library.LibraryQuery
import com.example.plugin_common.plugin.MediaPlugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
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
}

@OptIn(ExperimentalCoroutinesApi::class)
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

    private val _mode = MutableStateFlow<LibraryMode>(LibraryMode.Query(QueryMode.ALL))
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
                    val filteredItems = itemsForMode(items, mode)
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

    private val baseQueryItems by lazy {
        combine(
            _currentPlugin.flatMapLatest { plugin ->
                plugin?.getLibraryItems(LibraryQuery()) ?: flowOf(PagingData.Companion.empty())
            },
            itemMap
        ) { pagingData, items ->
            pagingData.map { remoteItem ->
                // Merge with cached item but preserve the query index from remote
                val cachedItem = items[remoteItem.id]
                if (cachedItem != null) {
                    cachedItem.copy(index = remoteItem.index)
                } else {
                    remoteItem
                }
            }
        }.cachedIn(scope)
    }
    private val filteredQueryItems by lazy {
        combine(baseQueryItems, itemMap) { pagingData, items ->
            pagingData
                .map { remoteItem -> items[remoteItem.id] ?: remoteItem }
                .filter { it.status == LibraryItemStatus.NONE }
        }.cachedIn(scope)
    }
    override val queryItems by lazy {
        _mode.flatMapLatest { mode ->
            when (mode) {
                is LibraryMode.Query -> {
                    if (mode.mode == QueryMode.NEW_ONLY) filteredQueryItems
                    else baseQueryItems
                }
                is LibraryMode.List -> flowOf(PagingData.empty())
            }
        }
    }

    private fun itemsForMode(
        items: Map<String, LibraryItem>,
        mode: LibraryMode
    ): List<LibraryItem> = when (mode) {
        is LibraryMode.Query -> emptyList()
        is LibraryMode.List -> {
            when (mode.mode) {
                ListMode.VIEWED -> items.values.filter { it.status == LibraryItemStatus.VIEWED }
                ListMode.LIKED -> items.values.filter { it.status == LibraryItemStatus.LIKED }
                ListMode.SAVED -> items.values.filter { it.saved }
            }
        }
    }

    private fun sortForMode(
        items: List<LibraryItem>,
        mode: LibraryMode
    ) = when (mode) {
        is LibraryMode.Query -> items.sortedBy { it.index }
        is LibraryMode.List -> {
            if (mode.sortMode == SortMode.BY_ACCESS) {
                items.sortedByDescending { it.lastAccessed }
            } else {
                items.sortedBy { it.index }
            }
        }
    }

    override val listItems by lazy {
        combine(itemMap, _mode, _currentPage) { items, mode, page ->
            val items = itemsForMode(items, mode)
            val sortedItems = sortForMode(items, mode)

            val pagedItems = sortedItems
                .drop(page * LibraryConstants.PAGE_SIZE)
                .take(LibraryConstants.PAGE_SIZE)

            PagingData.from(pagedItems)
        }.cachedIn(scope)
    }
}