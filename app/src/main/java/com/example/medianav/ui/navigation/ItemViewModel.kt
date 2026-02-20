package com.example.medianav.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medianav.library.LibraryManager
import com.example.plugin_common.library.LibraryItem
import com.example.plugin_common.library.LibraryItemStatus
import com.example.plugin_common.plugin.MediaPlugin
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ItemViewModel : ViewModel() {
    private val _currentPlugin = MutableStateFlow<MediaPlugin?>(null)
    val currentPlugin = _currentPlugin.asStateFlow()

    private val _selectedItem = MutableStateFlow<LibraryItem?>(null)
    val selectedItem = _currentPlugin.flatMapLatest { plugin ->
        if (plugin == null) flowOf(null)
        else {
            combine(
                _selectedItem,
                LibraryManager.itemsForPlugin(plugin)
            ) { base, dbMap ->
                if (base == null) null
                else dbMap[base.id] ?: base
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )

    fun setItem(item: LibraryItem, plugin: MediaPlugin) {
        _currentPlugin.value = plugin
        _selectedItem.value = item
    }

    fun toggleStatus(status: LibraryItemStatus) {
        val item = selectedItem.value ?: return
        val plugin = _currentPlugin.value ?: return

        viewModelScope.launch {
            val newStatus = if (item.status == status) LibraryItemStatus.NONE else status
            LibraryManager.setStatus(plugin, item, newStatus)
            _selectedItem.value = item.copy(status = newStatus)
        }
    }

    fun toggleSaved() {
        val item = selectedItem.value ?: return
        val plugin = _currentPlugin.value ?: return

        viewModelScope.launch {
            val newSaved = !item.saved
            val newItem = item.copy(
                saved = newSaved,
                lastAccessed = System.currentTimeMillis()
            )
            LibraryManager.addItem(plugin, newItem)
        }
    }
}