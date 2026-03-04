package com.example.medianav.ui.library.list

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.medianav.library.LibraryConstants
import com.example.medianav.ui.library.LibraryViewModel
import com.example.medianav.ui.library.mode.LibraryMode
import com.example.plugin_common.library.LibraryItem
import com.example.plugin_common.paging.LibraryPager
import com.example.plugin_common.paging.PagingConstants
import com.example.plugin_common.plugin.MediaPlugin
import kotlinx.coroutines.flow.distinctUntilChanged

@SuppressLint("MutableCollectionMutableState")
@Composable
internal fun LibraryGrid(
    viewModel: LibraryViewModel,
    plugin: MediaPlugin?,
    mode: LibraryMode,
    onItemClick: (LibraryItem, List<LibraryItem>) -> Unit
) {
    val pager = when (mode) {
        is LibraryMode.Query -> viewModel.queryPager.collectAsState()
        is LibraryMode.List -> viewModel.listPager.collectAsState()
    }.value

    if (plugin != null && pager != null) {
        PagingGrid(
            pager = pager,
            plugin = plugin,
            onItemClick = onItemClick
        )
    }
}

@Composable
private fun PagingGrid(
    pager: LibraryPager<LibraryItem>,
    plugin: MediaPlugin,
    onItemClick: (LibraryItem, List<LibraryItem>) -> Unit
) {
    val lazyPagingItems = pager.flow().collectAsLazyPagingItems()
    val gridState = rememberLazyGridState()
    val currentPage by pager.currentPage.collectAsState()
    val totalPages by pager.totalPageCount.collectAsState()

    // Track the page based on the first visible loaded item, not scroll position
    LaunchedEffect(gridState, lazyPagingItems.itemSnapshotList) {
        snapshotFlow {
            val firstVisibleIndex = gridState.firstVisibleItemIndex
            val firstVisibleItem = lazyPagingItems.itemSnapshotList.items.getOrNull(firstVisibleIndex)
            firstVisibleItem
        }
            .distinctUntilChanged()
            .collect { firstVisibleItem ->
                if (firstVisibleItem != null && totalPages > 0) {
                    val calculatedPage = firstVisibleItem.index / PagingConstants.Library.PAGE_SIZE
                    val validPage = calculatedPage.coerceIn(0, totalPages - 1)

                    if (validPage != currentPage) {
                        pager.setCurrentPage(validPage)
                    }
                }
            }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(
                count = lazyPagingItems.itemCount,
                key = { index ->
                    val item = lazyPagingItems.peek(index)
                    item?.id ?: "placeholder_$index"
                }
            ) { index ->
                val item = lazyPagingItems[index]
                Box(modifier = Modifier.padding(4.dp)) {
                    if (item != null) {
                        LibraryCell(
                            item = item,
                            plugin = plugin,
                            onClick = {
                                // Only collect items that are already loaded without triggering new loads
                                val loadedItems = (0 until lazyPagingItems.itemCount)
                                    .mapNotNull { lazyPagingItems.peek(it) }
                                onItemClick(item, loadedItems)
                            }
                        )
                    }
                }
            }
        }

        if (totalPages > 1) {
            PageBar(
                currentPage = currentPage,
                totalPages = totalPages,
                onPageChange = { page -> pager.jumpToPage(page) }
            )
        }
    }
}

