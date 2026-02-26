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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.medianav.library.LibraryConstants
import com.example.medianav.ui.library.LibraryViewModel
import com.example.medianav.ui.library.mode.LibraryMode
import com.example.plugin_common.library.LibraryItem
import com.example.plugin_common.paging.LibraryPager
import com.example.plugin_common.plugin.MediaPlugin
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

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

    if (pager != null) {
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
    plugin: MediaPlugin?,
    onItemClick: (LibraryItem, List<LibraryItem>) -> Unit
) {
    val lazyPagingItems = pager.flow().collectAsLazyPagingItems()
    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    val currentPage by pager.currentPage.collectAsState()
    val totalPages by pager.totalPageCount.collectAsState()

    LaunchedEffect(gridState) {
        snapshotFlow {
            val firstVisibleIndex = gridState.firstVisibleItemIndex
            firstVisibleIndex / LibraryConstants.PAGE_SIZE
        }
            .distinctUntilChanged()
            .collect { page ->
                pager.setCurrentPage(page)
            }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            state = gridState,
            contentPadding = PaddingValues(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(
                count = lazyPagingItems.itemCount,
                key = { index ->
                    val item = lazyPagingItems[index]
                    item?.id ?: "placeholder_$index"
                }
            ) { index ->
                val item = lazyPagingItems[index]
                Box(modifier = Modifier.padding(4.dp)) {
                    if (item != null && plugin != null) {
                        LibraryCell(
                            item = item,
                            plugin = plugin,
                            onClick = {
                                // Collect all currently loaded items
                                val loadedItems = (0 until lazyPagingItems.itemCount)
                                    .mapNotNull { lazyPagingItems[it] }
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
                onPageChange = { page ->
                    pager.setCurrentPage(page)
                    // Scroll to the target page
                    val targetIndex = page * LibraryConstants.PAGE_SIZE
                    coroutineScope.launch {
                        gridState.animateScrollToItem(targetIndex)
                    }
                }
            )
        }
    }
}

