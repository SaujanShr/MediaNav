package com.example.medianav.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.plugin_common.library.LibraryItem
import com.example.plugin_common.plugin.MediaPlugin
import kotlinx.coroutines.launch

@Composable
internal fun QueryMode(
    viewModel: LibraryViewModel,
    plugin: MediaPlugin?,
    onItemClick: (LibraryItem, List<LibraryItem>) -> Unit
) {
    val items = viewModel.queryItems.collectAsLazyPagingItems()
    val loadedItems = remember { mutableStateListOf<LibraryItem>() }
    val currentItem by viewModel.currentItem.collectAsState()
    val savedScrollIndex by viewModel.queryScrollIndex.collectAsState()
    val savedScrollOffset by viewModel.queryScrollOffset.collectAsState()
    val gridState = rememberLazyGridState(
        initialFirstVisibleItemIndex = savedScrollIndex,
        initialFirstVisibleItemScrollOffset = savedScrollOffset
    )
    val coroutineScope = rememberCoroutineScope()

    // Track loaded items
    LaunchedEffect(items.itemCount) {
        loadedItems.clear()
        for (i in 0 until items.itemCount) {
            items[i]?.let { loadedItems.add(it) }
        }
    }

    // Save scroll position whenever it changes (both index and offset)
    LaunchedEffect(gridState) {
        snapshotFlow {
            gridState.firstVisibleItemIndex to gridState.firstVisibleItemScrollOffset
        }.collect { (index, offset) ->
            viewModel.setQueryScrollPosition(index, offset)
        }
    }

    // Restore scroll position when coming back from media view
    LaunchedEffect(currentItem) {
        currentItem?.let { item ->
            val itemIndex = findItemIndex(items, item)

            if (itemIndex != null && itemIndex >= 0) {
                // Scroll to the last viewed item
                coroutineScope.launch {
                    gridState.scrollToItem(itemIndex)
                }

                // Preload items around current position
                preloadItemsAround(items, loadedItems, item.index)
            }
        }
    }

    // ...existing code...

    if (items.itemCount == 0 && items.loadState.refresh is LoadState.NotLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No items found")
        }
    } else {
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(items.itemCount) { index ->
                val item = items[index]
                if (item != null) {
                    plugin?.let {
                        LibraryItemCell(
                            item = item,
                            plugin = it,
                            onClick = {
                                onItemClick(item, loadedItems.sortedBy { it.index })
                            }
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.7f)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                    )
                }
            }
        }
    }
}

private fun findItemIndex(
    items: androidx.paging.compose.LazyPagingItems<LibraryItem>,
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
    items: androidx.paging.compose.LazyPagingItems<LibraryItem>,
    loadedItems: MutableList<LibraryItem>,
    currentIndex: Int
) {
    // Preload 5 items before and after current position
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

