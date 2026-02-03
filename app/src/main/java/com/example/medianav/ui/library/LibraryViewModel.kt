package com.example.medianav.ui.library

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medianav.library.LibraryConstants
import com.example.medianav.library.LibraryManager
import com.example.plugin_common.library.LibraryItem
import com.example.plugin_common.library.LibraryItemStatus
import com.example.plugin_common.library.LibraryQuery
import com.example.plugin_common.plugin.MediaPlugin
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LibraryViewModel : ViewModel() {
    private val _errors = MutableSharedFlow<String>()
    val errors = _errors.asSharedFlow()

    private val _currentPlugin = MutableStateFlow<MediaPlugin?>(null)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _expandedItemId = MutableStateFlow<String?>(null)
    val expandedItemId: StateFlow<String?> = _expandedItemId.asStateFlow()

    private val _selectedStatus = MutableStateFlow(LibraryItemStatus.NONE)
    val selectedStatus: StateFlow<LibraryItemStatus> = _selectedStatus.asStateFlow()

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val itemMap = _currentPlugin.flatMapLatest { plugin ->
        if (plugin == null) flowOf(emptyMap())
        else LibraryManager.itemsForPlugin(plugin)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyMap()
    )

    private val remotePageItems = MutableStateFlow<List<LibraryItem>>(emptyList())
    private val remoteTotalCount = MutableStateFlow(0)

    private val likedItems: StateFlow<List<LibraryItem>> =
        itemMap.map { map ->
            map.values.filter {
                it.status == LibraryItemStatus.LIKED
            }.sortedBy { it.id }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val viewedItems: StateFlow<List<LibraryItem>> =
        itemMap.map { map ->
            map.values.filter {
                it.status == LibraryItemStatus.VIEWED
            }.sortedBy { it.id }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val totalPages: StateFlow<Int> = combine(
        _selectedStatus,
        remoteTotalCount,
        likedItems,
        viewedItems
    ) { status, remoteCount, liked, viewed ->
        val totalItems = when (status) {
            LibraryItemStatus.NONE -> remoteCount
            LibraryItemStatus.LIKED -> liked.size
            LibraryItemStatus.VIEWED -> viewed.size
        }
        (totalItems + LibraryConstants.PAGE_SIZE - 1) / LibraryConstants.PAGE_SIZE
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = 0
    )

    val items: StateFlow<List<LibraryItem>> = combine(
        _selectedStatus,
        _currentPage,
        itemMap,
        remotePageItems
    ) { status, page, map, remote ->
        when (status) {
            LibraryItemStatus.NONE -> {
                remote.map { item ->
                    map[item.id] ?: item
                }
            }
            LibraryItemStatus.LIKED -> {
                map.values.filter { it.status == LibraryItemStatus.LIKED }
                    .sortedBy { it.id }
                    .drop(page * LibraryConstants.PAGE_SIZE)
                    .take(LibraryConstants.PAGE_SIZE)
            }
            LibraryItemStatus.VIEWED -> {
                map.values.filter { it.status == LibraryItemStatus.VIEWED }
                    .sortedBy { it.id }
                    .drop(page * LibraryConstants.PAGE_SIZE)
                    .take(LibraryConstants.PAGE_SIZE)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun setPlugin(plugin: MediaPlugin?) {
        // Only reset if the plugin has actually changed.
        // This prevents the page number from resetting during orientation changes (rotation).
        if (_currentPlugin.value?.info?.id == plugin?.info?.id) return

        _currentPlugin.value = plugin
        _currentPage.value = 0
        _selectedStatus.value = LibraryItemStatus.NONE
    }

    fun setStatus(status: LibraryItemStatus) {
        _selectedStatus.value = status
        _currentPage.value = 0
    }

    fun setPage(page: Int) {
        _currentPage.value = page
    }

    fun loadLibraryPage() {
        val plugin = _currentPlugin.value ?: return
        if (_selectedStatus.value != LibraryItemStatus.NONE) return

        viewModelScope.launch {
            _isLoading.value = true

            try {
                val offset = _currentPage.value * LibraryConstants.PAGE_SIZE
                val query = LibraryQuery()
                val fetchedItems = plugin.getLibraryItems(offset, LibraryConstants.PAGE_SIZE, query)

                remotePageItems.value = fetchedItems
                
                fetchedItems.forEach { item ->
                    LibraryManager.addItemToCache(plugin, item)
                }
            } catch (e: Exception) {
                _errors.emit(e.message ?: "Failed to load page")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadLibraryPageNumbers() {
        val plugin = _currentPlugin.value ?: return
        if (_selectedStatus.value != LibraryItemStatus.NONE) return

        viewModelScope.launch {
            try {
                val query = LibraryQuery()
                val count = plugin.getLibraryItemCount(query)
                remoteTotalCount.value = count
            } catch (e: Exception) {
                _errors.emit(e.message ?: "Failed to load page numbers")
            }
        }
    }

    fun toggleExpanded(itemId: String) {
        _expandedItemId.value = if (_expandedItemId.value == itemId) null else itemId
    }

    fun toggleLikeItem(context: Context, item: LibraryItem) {
        val plugin = _currentPlugin.value ?: return
        viewModelScope.launch {
            val newStatus = if (item.status == LibraryItemStatus.LIKED) {
                LibraryItemStatus.NONE
            } else {
                LibraryItemStatus.LIKED
            }
            LibraryManager.setItemStatus(context, plugin, item, newStatus)
        }
    }

    fun toggleViewItem(context: Context, item: LibraryItem) {
        val plugin = _currentPlugin.value ?: return
        viewModelScope.launch {
            val newStatus = if (item.status == LibraryItemStatus.VIEWED) {
                LibraryItemStatus.NONE
            } else {
                LibraryItemStatus.VIEWED
            }
            LibraryManager.setItemStatus(context, plugin, item, newStatus)
        }
    }
}