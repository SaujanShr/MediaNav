package com.example.medianav.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medianav.library.LibraryManager
import com.example.medianav.ui.library.list.ListState
import com.example.medianav.ui.library.list.ListStateProvider
import com.example.medianav.ui.library.media.MediaState
import com.example.medianav.ui.library.media.MediaStateProvider
import com.example.plugin_common.library.LibraryItemStatus
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val listState: ListState = ListState(),
    private val mediaState: MediaState = MediaState()
):
    ViewModel(),
    ListStateProvider by listState,
    MediaStateProvider by mediaState {
    init {
        listState.initialize(viewModelScope)
        mediaState.initialize(viewModelScope)
    }

    fun toggleStatus(status: LibraryItemStatus) {
        val plugin = currentPlugin.value ?: return
        val item = currentItem.value ?: return

        viewModelScope.launch {
            val newStatus = if (item.status == status) LibraryItemStatus.NONE else status
            LibraryManager.setStatus(plugin, item, newStatus)
        }
    }

    fun toggleSaved() {
        val item = currentItem.value ?: return
        val plugin = listState.currentPlugin.value ?: return

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
