package com.example.medianav.ui.library.media

import com.example.plugin_common.library.LibraryItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

interface MediaStateProvider {
    val currentItem: StateFlow<LibraryItem?>
    val canNavigateNext: StateFlow<Boolean>
    val canNavigatePrevious: StateFlow<Boolean>

    fun selectItem(item: LibraryItem, itemsList: List<LibraryItem>)
    fun clearItem()
    fun navigateNext()
    fun navigatePrevious()
}

class MediaState : MediaStateProvider {
    private lateinit var scope: CoroutineScope

    fun initialize(scope: CoroutineScope) {
        this.scope = scope
    }

    private val _currentItem = MutableStateFlow<LibraryItem?>(null)
    override val currentItem = _currentItem.asStateFlow()

    private val _currentItemsList = MutableStateFlow<List<LibraryItem>>(emptyList())
    private val _currentItemIndex = MutableStateFlow(0)

    override val canNavigateNext by lazy {
        combine(_currentItemsList, _currentItemIndex) { items, index ->
            index < items.size - 1
        }.stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )
    }

    override val canNavigatePrevious by lazy {
        combine(_currentItemsList, _currentItemIndex) { _, index ->
            index > 0
        }.stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )
    }

    override fun selectItem(item: LibraryItem, itemsList: List<LibraryItem>) {
        _currentItem.value = item
        _currentItemsList.value = itemsList
        _currentItemIndex.value = itemsList.indexOfFirst { it.id == item.id }.coerceAtLeast(0)
    }

    override fun clearItem() {
        _currentItem.value = null
        _currentItemsList.value = emptyList()
        _currentItemIndex.value = 0
    }

    override fun navigateNext() {
        val items = _currentItemsList.value
        val currentIndex = _currentItemIndex.value
        if (currentIndex < items.size - 1) {
            _currentItemIndex.value = currentIndex + 1
            _currentItem.value = items[currentIndex + 1]
        }
    }

    override fun navigatePrevious() {
        val currentIndex = _currentItemIndex.value
        val items = _currentItemsList.value
        if (currentIndex > 0) {
            _currentItemIndex.value = currentIndex - 1
            _currentItem.value = items[currentIndex - 1]
        }
    }
}