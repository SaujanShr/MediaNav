package com.example.medianav.ui.library.list

import com.example.custom_paging.paging.Pager
import com.example.custom_paging.paging.PagingSource
import com.example.custom_paging.paging.PagingResult
import com.example.custom_paging.paging.PagingItem
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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

interface ListStateProvider {
    val currentPlugin: StateFlow<MediaPlugin?>
    val mode: StateFlow<LibraryMode>
    val currentPage: StateFlow<Int>
    val totalPages: StateFlow<Int>
    val libraryQuery: StateFlow<LibraryQuery?>
    val queryPager: StateFlow<Pager<LibraryItem>?>
    val listPager: StateFlow<Pager<LibraryItem>?>

    fun setPlugin(plugin: MediaPlugin?)
    fun setMode(mode: LibraryMode)
    fun setPage(page: Int)
    fun setLibraryQuery(query: LibraryQuery?)
    fun toggleEditMode()
    suspend fun moveItem(item: LibraryItem, fromIndex: Int, toIndex: Int)
}

class ListState: ListStateProvider {
    private lateinit var scope: CoroutineScope

    fun initialize(scope: CoroutineScope) {
        this.scope = scope

        scope.launch {
            queryPager.collect { pager ->
                if (pager != null) {
                    launch {
                        pager.totalPages.collect { total ->
                            _queryTotalPages.value = total
                        }
                    }
                } else {
                    _queryTotalPages.value = 0
                }
            }
        }

        scope.launch {
            listPager.collect { pager ->
                if (pager != null) {
                    launch {
                        pager.totalPages.collect { total ->
                            _listTotalPages.value = total
                        }
                    }
                } else {
                    _listTotalPages.value = 0
                }
            }
        }
    }

    private val _currentPlugin = MutableStateFlow<MediaPlugin?>(null)
    override val currentPlugin = _currentPlugin.asStateFlow()

    override fun setPlugin(plugin: MediaPlugin?) {
        _currentPlugin.value = plugin
    }

    private val _libraryQuery = MutableStateFlow<LibraryQuery?>(null)
    override val libraryQuery = _libraryQuery.asStateFlow()

    override fun setLibraryQuery(query: LibraryQuery?) {
        _libraryQuery.value = query
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

    override val totalPages: StateFlow<Int> by lazy {
        combine(_mode, _listTotalPages, _queryTotalPages) { mode, listTotal, queryTotal ->
            when (mode) {
                is LibraryMode.Query -> queryTotal
                is LibraryMode.List -> listTotal
            }
        }.stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )
    }

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

    private val _queryTotalPages = MutableStateFlow(0)
    private val _listTotalPages = MutableStateFlow(0)

    override val queryPager: StateFlow<Pager<LibraryItem>?> by lazy {
        combine(_currentPlugin, _libraryQuery) { plugin, query ->
            if (plugin != null) {
                val pager = plugin.getPager(query)
                pager.initialize(scope)
                pager
            } else null
        }.stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )
    }

    override val listPager: StateFlow<Pager<LibraryItem>?> by lazy {
        combine(itemMap, _mode) { items, mode ->
            if (mode is LibraryMode.List) {
                val pager = getListPager(items, mode)
                pager.initialize(scope)
                pager
            } else null
        }.stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )
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

    override fun toggleEditMode() {
        val current = _mode.value
        if (current is LibraryMode.List) {
            _mode.value = current.copy(isEdit = !current.isEdit)
        }
    }

    override suspend fun moveItem(item: LibraryItem, fromIndex: Int, toIndex: Int) {
        val plugin = _currentPlugin.value ?: return
        val mode = _mode.value

        if (mode !is LibraryMode.List) return

        val items = itemMap.value
        val filteredItems = listItemsForMode(items, mode)
        val sortedItems = sortForMode(filteredItems, mode).toMutableList()

        val itemToMove = sortedItems.find { it.id == item.id } ?: return
        val oldPosition = sortedItems.indexOf(itemToMove)

        if (oldPosition == -1 || oldPosition == toIndex) return

        sortedItems.removeAt(oldPosition)
        sortedItems.add(toIndex, itemToMove)

        sortedItems.forEachIndexed { index, libraryItem ->
            if (libraryItem.index != index) {
                LibraryManager.setIndex(plugin, libraryItem, index)
            }
        }
    }
}
