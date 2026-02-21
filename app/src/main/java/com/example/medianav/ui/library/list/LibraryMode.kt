package com.example.medianav.ui.library.list

import com.example.plugin_common.library.LibraryItemStatus

// Query modes - for browsing new content from plugins
enum class QueryMode {
    ALL,      // Show all query results
    NEW_ONLY  // Filter out items already in library
}

// List modes - for viewing library items by status
enum class ListMode {
    VIEWED,
    LIKED,
    SAVED
}

// Sort options for list modes
enum class SortMode {
    BY_INDEX,      // Original order
    BY_ACCESS      // Sort by last accessed time
}

// Whether list is in edit mode
enum class EditMode {
    NORMAL,
    EDIT
}

// Main mode - either querying or viewing a list
sealed class LibraryMode {
    data class Query(val mode: QueryMode) : LibraryMode()
    data class List(
        val mode: ListMode,
        val sortMode: SortMode = SortMode.BY_INDEX,
        val editMode: EditMode = EditMode.NORMAL
    ) : LibraryMode()
}

// Extension helpers
val LibraryMode.isQuery: Boolean get() = this is LibraryMode.Query
val LibraryMode.isList: Boolean get() = this is LibraryMode.List
val LibraryMode.isEdit: Boolean get() = (this as? LibraryMode.List)?.editMode == EditMode.EDIT

// Map ListMode to LibraryItemStatus
fun ListMode.toStatus(): LibraryItemStatus = when (this) {
    ListMode.VIEWED -> LibraryItemStatus.VIEWED
    ListMode.LIKED -> LibraryItemStatus.LIKED
    ListMode.SAVED -> LibraryItemStatus.NONE  // Saved items use NONE status with saved flag
}


