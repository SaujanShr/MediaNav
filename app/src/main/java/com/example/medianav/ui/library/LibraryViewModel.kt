package com.example.medianav.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.collections.filter
import kotlin.collections.sortedBy
import kotlin.collections.sortedByDescending

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModel : ViewModel() {
    private val _errors = MutableSharedFlow<String>()
    val errors = _errors.asSharedFlow()

    private val _currentPlugin = MutableStateFlow<MediaPlugin?>(null)
    val currentPlugin = _currentPlugin.asStateFlow()

    private val _selectedStatus = MutableStateFlow(LibraryItemStatus.NONE)
    val selectedStatus = _selectedStatus.asStateFlow()

    private val _currentPage = MutableStateFlow(0)
    val currentPage = _currentPage.asStateFlow()

    private val _totalPages = MutableStateFlow(1)
    val totalPages = _totalPages.asStateFlow()

    private val _mode = MutableStateFlow(LibraryMode.QUERY)
    val mode = _mode.asStateFlow()

    private val _selectedItem = MutableStateFlow<LibraryItem?>(null)
    val selectedItem = _selectedItem.asStateFlow()

    private val _currentItemsList = MutableStateFlow<List<LibraryItem>>(emptyList())
    private val _currentItemIndex = MutableStateFlow(0)

    val canNavigateNext = combine(_currentItemsList, _currentItemIndex) { items, index ->
        index < items.size - 1
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false
    )

    val canNavigatePrevious = _currentItemIndex.map { it > 0 }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false
    )

    private val itemMap = _currentPlugin.flatMapLatest { plugin ->
        if (plugin == null) flowOf(emptyMap())
        else LibraryManager.itemsForPlugin(plugin)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyMap()
    )

    private val _baseQueryItems = _currentPlugin.flatMapLatest { plugin ->
        plugin?.getLibraryItems(LibraryQuery()) ?: flowOf(PagingData.empty())
    }.cachedIn(viewModelScope)

    private val _filteredQueryItems = combine(_baseQueryItems, itemMap) { pagingData, items ->
        pagingData
            .map { remoteItem -> items[remoteItem.id] ?: remoteItem }
            .filter { it.status == LibraryItemStatus.NONE }
    }.cachedIn(viewModelScope)

    val queryItems: Flow<PagingData<LibraryItem>> = mode.flatMapLatest { mode ->
        if (mode == LibraryMode.QUERY_NEW_ONLY) _filteredQueryItems else _baseQueryItems
    }

    val listItems: Flow<PagingData<LibraryItem>> = combine(
        itemMap,
        _selectedStatus,
        _mode,
        _currentPage
    ) { items, status, mode, page ->
        val filtered = filterListMode(items, status, mode)

        _totalPages.value =
            ((filtered.size + LibraryConstants.PAGE_SIZE - 1) / LibraryConstants.PAGE_SIZE)
                .coerceAtLeast(1)

        val pagedItems = filtered
            .drop(page * LibraryConstants.PAGE_SIZE)
            .take(LibraryConstants.PAGE_SIZE)

        PagingData.from(pagedItems)
    }.cachedIn(viewModelScope)

    fun setPlugin(plugin: MediaPlugin?) {
        if (_currentPlugin.value?.metadata?.id == plugin?.metadata?.id) return
        _currentPlugin.value = plugin
        _selectedStatus.value = LibraryItemStatus.NONE
        _mode.value = LibraryMode.QUERY
        _currentPage.value = 0
    }

    fun setStatus(status: LibraryItemStatus) {
        _selectedStatus.value = status
        _mode.value =
            if (_selectedStatus.value == LibraryItemStatus.NONE) {
                if (status == LibraryItemStatus.NONE) {
                    if (_mode.value == LibraryMode.QUERY) LibraryMode.QUERY_NEW_ONLY
                    else LibraryMode.QUERY
                } else {
                    LibraryMode.QUERY
                }
            } else {
                LibraryMode.LIST
            }
        _currentPage.value = 0
    }

    fun setSavedMode() {
        _selectedStatus.value = LibraryItemStatus.NONE
        _mode.value = LibraryMode.SAVED
        _currentPage.value = 0
    }

    fun toggleSavedSortOrder() {
        _mode.value = when (_mode.value) {
            LibraryMode.SAVED -> LibraryMode.SAVED_BY_DATE
            LibraryMode.SAVED_BY_DATE -> LibraryMode.SAVED
            else -> LibraryMode.SAVED
        }
        _currentPage.value = 0
    }

    fun toggleEditMode() {
        _mode.value =
            if (_mode.value == LibraryMode.LIST) LibraryMode.EDIT
            else LibraryMode.LIST
    }

    fun setPage(page: Int) {
        _currentPage.value = page
    }

    fun selectItem(item: LibraryItem, itemsList: List<LibraryItem>) {
        _selectedItem.value = item
        _currentItemsList.value = itemsList
        _currentItemIndex.value = itemsList.indexOfFirst { it.id == item.id }.coerceAtLeast(0)
    }

    fun clearSelectedItem() {
        _selectedItem.value = null
        _currentItemsList.value = emptyList()
        _currentItemIndex.value = 0
    }

    fun navigateNext() {
        val items = _currentItemsList.value
        val currentIndex = _currentItemIndex.value
        if (currentIndex < items.size - 1) {
            _currentItemIndex.value = currentIndex + 1
            _selectedItem.value = items[currentIndex + 1]
        }
    }

    fun navigatePrevious() {
        val currentIndex = _currentItemIndex.value
        val items = _currentItemsList.value
        if (currentIndex > 0) {
            _currentItemIndex.value = currentIndex - 1
            _selectedItem.value = items[currentIndex - 1]
        }
    }

    fun toggleStatus(status: LibraryItemStatus) {
        val item = _selectedItem.value ?: return
        val plugin = _currentPlugin.value ?: return

        viewModelScope.launch {
            val newStatus = if (item.status == status) LibraryItemStatus.NONE else status
            LibraryManager.setStatus(plugin, item, newStatus)
            _selectedItem.value = item.copy(status = newStatus)
        }
    }

    fun toggleSaved() {
        val item = _selectedItem.value ?: return
        val plugin = _currentPlugin.value ?: return

        viewModelScope.launch {
            val newSaved = !item.saved
            val newItem = item.copy(
                saved = newSaved,
                lastAccessed = System.currentTimeMillis()
            )
            LibraryManager.addItem(plugin, newItem)
            _selectedItem.value = newItem
        }
    }

    private fun filterListMode(items: Map<String, LibraryItem>, status: LibraryItemStatus, mode: LibraryMode) = when (mode) {
        LibraryMode.SAVED -> {
                items.values.filter { it.saved }.sortedBy { it.index }
        }
        LibraryMode.SAVED_BY_DATE -> {
            items.values.filter { it.saved }.sortedByDescending { it.lastAccessed }
        }
        LibraryMode.LIST, LibraryMode.EDIT -> {
            items.values.filter { it.status == status }.sortedBy { it.index }
        }
        else -> {
            items.values.toList()
        }
    }
}
