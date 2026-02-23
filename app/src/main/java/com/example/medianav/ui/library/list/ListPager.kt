package com.example.medianav.ui.library.list

import com.example.custom_paging.paging.Pager
import com.example.custom_paging.paging.PagingItem
import com.example.custom_paging.paging.PagingResult
import com.example.custom_paging.paging.createPagingSource
import com.example.medianav.library.LibraryConstants
import com.example.medianav.ui.library.mode.LibraryMode
import com.example.medianav.ui.library.mode.ListModeSort
import com.example.medianav.ui.library.mode.ListModeStatus
import com.example.plugin_common.library.LibraryItem
import com.example.plugin_common.library.LibraryItemStatus

fun getListPager(items: Map<String, LibraryItem>, mode: LibraryMode.List): Pager<LibraryItem> {
    val filteredItems = listItemsForMode(items, mode)
    val sortedItems = sortForMode(filteredItems, mode)

    val pager = Pager(
        createPagingSource(
            loadSize = LibraryConstants.PAGE_SIZE,
            loader = { startIndex ->
                val pageSize = LibraryConstants.PAGE_SIZE
                val endIndex = minOf(startIndex + pageSize, sortedItems.size)

                if (startIndex >= sortedItems.size) {
                    PagingResult.Success(emptyList(), sortedItems.size)
                } else {
                    val pageItems = sortedItems.subList(startIndex, endIndex)
                        .mapIndexed { offset, item ->
                            PagingItem(item, startIndex + offset)
                        }

                    PagingResult.Success(pageItems, sortedItems.size)
                }
            }
        )
    )

    return pager
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