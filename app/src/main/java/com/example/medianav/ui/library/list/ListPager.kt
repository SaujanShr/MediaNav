package com.example.medianav.ui.library.list

import com.example.plugin_common.paging.LibraryPager
import com.example.plugin_common.paging.createLibraryListPager
import com.example.medianav.library.LibraryConstants
import com.example.medianav.ui.library.mode.LibraryMode
import com.example.medianav.ui.library.mode.ListModeSort
import com.example.medianav.ui.library.mode.ListModeStatus
import com.example.plugin_common.library.LibraryItem
import com.example.plugin_common.library.LibraryItemStatus

fun getListPager(items: Map<String, LibraryItem>, mode: LibraryMode.List): LibraryPager<LibraryItem> {
    val filteredItems = listItemsForMode(items, mode)
    val sortedItems = sortForMode(filteredItems, mode)

    return createLibraryListPager(
        items = sortedItems,
        pageSize = LibraryConstants.PAGE_SIZE,
        prefetchDistance = 5
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