package com.example.medianav.ui.library.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.medianav.library.LibraryConstants
import com.example.medianav.ui.library.LibraryViewModel
import com.example.medianav.ui.library.mode.LibraryMode
import com.example.plugin_common.library.LibraryItem
import com.example.plugin_common.plugin.MediaPlugin
import kotlinx.coroutines.launch

@Composable
internal fun LibraryGrid(
    viewModel: LibraryViewModel,
    plugin: MediaPlugin?,
    mode: LibraryMode,
    scrollIndex: Int,
    scrollOffset: Int,
    onScrollPositionChange: (Int, Int) -> Unit,
    onItemClick: (LibraryItem, List<LibraryItem>) -> Unit
) {
    val items = when (mode) {
        is LibraryMode.Query -> viewModel.queryItems.collectAsLazyPagingItems()
        is LibraryMode.List -> viewModel.listItems.collectAsLazyPagingItems()
    }

    val loadedItems = remember { mutableStateListOf<LibraryItem>() }
    val currentItem by viewModel.currentItem.collectAsState()
    val gridState = rememberLazyGridState(
        initialFirstVisibleItemIndex = scrollIndex,
        initialFirstVisibleItemScrollOffset = scrollOffset
    )
    val coroutineScope = rememberCoroutineScope()

    val itemsPerPage = LibraryConstants.PAGE_SIZE
    val totalPages = remember(items.itemCount) {
        if (items.itemCount == 0) 0
        else (items.itemCount + itemsPerPage - 1) / itemsPerPage
    }

    var currentPage by remember { mutableIntStateOf(0) }

    LaunchedEffect(gridState, items.itemCount) {
        snapshotFlow { gridState.firstVisibleItemIndex }
            .collect { firstVisibleIndex ->
                val newPage = if (items.itemCount == 0) 0
                else firstVisibleIndex / itemsPerPage

                if (newPage != currentPage) {
                    currentPage = newPage
                }
            }
    }

    val onPageChange: (Int) -> Unit = { page ->
        coroutineScope.launch {
            val targetIndex = page * itemsPerPage
            gridState.scrollToItem(targetIndex)
        }
    }


    // Track loaded items
    LaunchedEffect(items.itemCount) {
        loadedItems.clear()
        for (i in 0 until items.itemCount) {
            items[i]?.let { loadedItems.add(it) }
        }
    }

    // Save scroll position whenever it changes (both modes now)
    LaunchedEffect(gridState) {
        snapshotFlow {
            gridState.firstVisibleItemIndex to gridState.firstVisibleItemScrollOffset
        }.collect { (index, offset) ->
            onScrollPositionChange(index, offset)
        }
    }

    // Restore scroll position when coming back from media view
    LaunchedEffect(currentItem) {
        currentItem?.let { item ->
            val itemIndex = findItemIndex(items, item)

            if (itemIndex != null && itemIndex >= 0) {
                coroutineScope.launch {
                    gridState.scrollToItem(itemIndex)
                }

                preloadItemsAround(items, loadedItems, item.index)
            }
        }
    }

    // Unified grid with page bar for both modes
    GridWithPageBar(
        items = items,
        loadedItems = loadedItems,
        gridState = gridState,
        plugin = plugin,
        mode = mode,
        onItemClick = onItemClick,
        totalPages = totalPages,
        currentPage = currentPage,
        onPageChange = onPageChange
    )
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GridWithPageBar(
    items: LazyPagingItems<LibraryItem>,
    loadedItems: List<LibraryItem>,
    gridState: LazyGridState,
    plugin: MediaPlugin?,
    mode: LibraryMode,
    onItemClick: (LibraryItem, List<LibraryItem>) -> Unit,
    totalPages: Int,
    currentPage: Int,
    onPageChange: (Int) -> Unit
) {
    val isEditMode = mode is LibraryMode.List && mode.isEdit

    if (items.itemCount == 0 && items.loadState.refresh is LoadState.NotLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No items found")
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        count = items.itemCount,
                        key = { index -> items[index]?.id ?: index }
                    ) { index ->
                        val item = items[index]
                        if (item != null) {
                            plugin?.let {
                                LibraryItemCell(
                                    item = item,
                                    plugin = it,
                                    onClick = {
                                        if (!isEditMode) {
                                            onItemClick(item, loadedItems.sortedBy { it.index })
                                        }
                                    }
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(0.7f)
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(8.dp)
                                    )
                            )
                        }
                    }
                }
            }

            if (totalPages > 1) {
                PageBar(
                    currentPage = currentPage,
                    totalPages = totalPages,
                    onPageChange = onPageChange
                )
            }
        }
    }
}


private fun findItemIndex(
    items: LazyPagingItems<LibraryItem>,
    targetItem: LibraryItem
): Int? {
    for (i in 0 until items.itemCount) {
        val item = items.peek(i)
        if (item?.id == targetItem.id) {
            return i
        }
    }
    return null
}

private fun preloadItemsAround(
    items: LazyPagingItems<LibraryItem>,
    loadedItems: MutableList<LibraryItem>,
    currentIndex: Int
) {
    val startIndex = maxOf(0, currentIndex - 5)
    val endIndex = minOf(items.itemCount - 1, currentIndex + 5)

    for (i in startIndex..endIndex) {
        items[i]?.let { item ->
            if (loadedItems.none { it.id == item.id }) {
                loadedItems.add(item)
            }
        }
    }
}

