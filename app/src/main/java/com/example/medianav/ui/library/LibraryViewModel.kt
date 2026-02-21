package com.example.medianav.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medianav.library.LibraryManager
import com.example.medianav.ui.library.list.EditMode
import com.example.medianav.ui.library.list.LibraryMode
import com.example.medianav.ui.library.list.ListMode
import com.example.medianav.ui.library.list.ListState
import com.example.medianav.ui.library.list.ListStateProvider
import com.example.medianav.ui.library.list.QueryMode
import com.example.medianav.ui.library.list.SortMode
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

    // Mode manipulation functions
    fun setStatus(status: LibraryItemStatus) {
        when (status) {
            LibraryItemStatus.VIEWED -> setMode(LibraryMode.List(ListMode.VIEWED))
            LibraryItemStatus.LIKED -> setMode(LibraryMode.List(ListMode.LIKED))
            LibraryItemStatus.NONE -> setMode(LibraryMode.Query(QueryMode.ALL))
        }
    }

    fun setSavedMode() {
        setMode(LibraryMode.List(ListMode.SAVED))
    }

    fun toggleSavedSortOrder() {
        val currentMode = mode.value
        if (currentMode is LibraryMode.List && currentMode.mode == ListMode.SAVED) {
            val newSortMode = if (currentMode.sortMode == SortMode.BY_INDEX) {
                SortMode.BY_ACCESS
            } else {
                SortMode.BY_INDEX
            }
            setMode(LibraryMode.List(ListMode.SAVED, newSortMode, currentMode.editMode))
        }
    }

    fun toggleEditMode() {
        val currentMode = mode.value
        if (currentMode is LibraryMode.List) {
            val newEditMode = if (currentMode.editMode == EditMode.NORMAL) {
                EditMode.EDIT
            } else {
                EditMode.NORMAL
            }
            setMode(LibraryMode.List(currentMode.mode, currentMode.sortMode, newEditMode))
        }
    }

    // Item actions
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
