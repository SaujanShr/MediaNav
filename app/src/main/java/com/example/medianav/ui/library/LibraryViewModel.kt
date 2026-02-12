package com.example.medianav.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.map
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

enum class LibraryMode {
    QUERY, LIST
}

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModel : ViewModel() {
    private val _errors = MutableSharedFlow<String>()
    val errors = _errors.asSharedFlow()

    private val _currentPlugin = MutableStateFlow<MediaPlugin?>(null)
    val currentPlugin = _currentPlugin.asStateFlow()

    private val _selectedStatus = MutableStateFlow(LibraryItemStatus.NONE)
    val selectedStatus = _selectedStatus.asStateFlow()

    private val _isQueryFiltered = MutableStateFlow(false)
    val isQueryFiltered = _isQueryFiltered.asStateFlow()

    private val _currentPage = MutableStateFlow(0)
    val currentPage = _currentPage.asStateFlow()

    private val _totalPages = MutableStateFlow(1)
    val totalPages = _totalPages.asStateFlow()

    private val pageSize = 9

    val mode: Flow<LibraryMode> = _selectedStatus.map {
        if (it == LibraryItemStatus.NONE) LibraryMode.QUERY else LibraryMode.LIST
    }

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

    val queryItems: Flow<PagingData<LibraryItem>> = _isQueryFiltered.flatMapLatest { filtered ->
        if (filtered) _filteredQueryItems else _baseQueryItems
    }

    val listItems: Flow<PagingData<LibraryItem>> = combine(_selectedStatus, itemMap, _currentPage) { status, items, page ->
        if (status == LibraryItemStatus.NONE) {
            PagingData.empty()
        } else {
            val filtered = items.values
                .filter { it.status == status }
                .sortedBy { it.index }
            
            _totalPages.value = ((filtered.size + pageSize - 1) / pageSize).coerceAtLeast(1)
            
            val pagedItems = filtered.drop(page * pageSize).take(pageSize)
            PagingData.from(pagedItems)
        }
    }.cachedIn(viewModelScope)


    fun setPlugin(plugin: MediaPlugin?) {
        if (_currentPlugin.value?.metadata?.id == plugin?.metadata?.id) return
        _currentPlugin.value = plugin
        _selectedStatus.value = LibraryItemStatus.NONE
        _isQueryFiltered.value = false
        _currentPage.value = 0
    }

    fun setStatus(status: LibraryItemStatus) {
        if (_selectedStatus.value == LibraryItemStatus.NONE && status == LibraryItemStatus.NONE) {
            _isQueryFiltered.value = !_isQueryFiltered.value
        } else {
            _selectedStatus.value = status
            if (status != LibraryItemStatus.NONE) {
                _isQueryFiltered.value = false
            }
        }
        _currentPage.value = 0
    }

    fun setPage(page: Int) {
        _currentPage.value = page
    }
}
