package com.example.medianav.ui.library.list

import com.example.medianav.library.LibraryConstants
import com.example.medianav.library.LibraryManager
import com.example.plugin_common.paging.createLibraryListPager
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
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

interface ListStateProvider {
    val currentPlugin: StateFlow<MediaPlugin?>
    val mode: StateFlow<LibraryMode>
    val currentPage: StateFlow<Int>
    val totalPages: StateFlow<Int>
    val libraryQuery: StateFlow<LibraryQuery?>
    val queryPager: StateFlow<com.example.plugin_common.paging.LibraryPager<LibraryItem>?>
    val listPager: StateFlow<com.example.plugin_common.paging.LibraryPager<LibraryItem>?>

    fun setPlugin(plugin: MediaPlugin?)
    fun setMode(mode: LibraryMode)
    fun setLibraryQuery(query: LibraryQuery?)
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

    override val queryPager: StateFlow<com.example.plugin_common.paging.LibraryPager<LibraryItem>?> by lazy {
        combine(_currentPlugin, _libraryQuery) { plugin, query ->
            if (plugin != null) {
                plugin.getPager(query)
            } else null
        }.stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )
    }

    override val listPager: StateFlow<com.example.plugin_common.paging.LibraryPager<LibraryItem>?> by lazy {
        combine(itemMap, _mode) { items: Map<String, LibraryItem>, mode: LibraryMode ->
            // Create a stable key based on the sorted item IDs to detect real changes
            if (mode is LibraryMode.List) {
                val filteredItems = when (mode.status) {
                    ListModeStatus.VIEWED -> items.values.filter { it.status == LibraryItemStatus.VIEWED }
                    ListModeStatus.LIKED -> items.values.filter { it.status == LibraryItemStatus.LIKED }
                    ListModeStatus.SAVED -> items.values.filter { it.saved }
                }
                val sortedItems = when (mode.sort) {
                    ListModeSort.BY_ACCESS -> filteredItems.sortedByDescending { it.lastAccessed }
                    ListModeSort.BY_INDEX -> filteredItems.sortedBy { it.index }
                }
                // Use item IDs as the key to detect changes
                val itemsKey = sortedItems.map { it.id }.joinToString(",")
                Triple<List<LibraryItem>, LibraryMode, String>(sortedItems, mode, itemsKey)
            } else {
                Triple<List<LibraryItem>, LibraryMode, String>(emptyList(), mode, "")
            }
        }
            .distinctUntilChangedBy { (_, _, key) -> key } // Only recreate if the items key changes
            .map { (sortedItems, mode, _) ->
                if (mode is LibraryMode.List && sortedItems.isNotEmpty()) {
                    createLibraryListPager(
                        items = sortedItems,
                        pageSize = LibraryConstants.PAGE_SIZE,
                        prefetchDistance = 5
                    )
                } else null
            }
            .stateIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = null
            )
    }

    override val totalPages: StateFlow<Int> by lazy {
        combine(_mode, queryPager, listPager) { mode, queryP, listP ->
            when (mode) {
                is LibraryMode.Query -> queryP
                is LibraryMode.List -> listP
            }
        }.flatMapLatest { pager ->
            pager?.totalPageCount ?: flowOf(0)
        }.stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )
    }

    override val currentPage: StateFlow<Int> by lazy {
        combine(_mode, queryPager, listPager) { mode, queryP, listP ->
            when (mode) {
                is LibraryMode.Query -> queryP
                is LibraryMode.List -> listP
            }
        }.flatMapLatest { pager ->
            pager?.currentPage ?: flowOf(0)
        }.stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )
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
        val filteredItems = when (mode) {
            is LibraryMode.List -> {
                when (mode.status) {
                    ListModeStatus.VIEWED -> items.values.filter { it.status == LibraryItemStatus.VIEWED }
                    ListModeStatus.LIKED -> items.values.filter { it.status == LibraryItemStatus.LIKED }
                    ListModeStatus.SAVED -> items.values.filter { it.saved }
                }
            }
        }

        val sortedItems = when (mode) {
            is LibraryMode.List -> {
                if (mode.sort == ListModeSort.BY_ACCESS) {
                    filteredItems.sortedByDescending { it.lastAccessed }
                } else {
                    filteredItems.sortedBy { it.index }
                }
            }
        }.toMutableList()

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
