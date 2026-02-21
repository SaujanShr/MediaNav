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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.plugin_common.library.LibraryItem
import com.example.plugin_common.plugin.MediaPlugin

@Composable
internal fun QueryMode(
    viewModel: LibraryViewModel,
    plugin: MediaPlugin?,
    onItemClick: (LibraryItem, List<LibraryItem>) -> Unit
) {
    val items = viewModel.queryItems.collectAsLazyPagingItems()
    val loadedItems = remember { mutableStateListOf<LibraryItem>() }
    val currentItem by viewModel.currentItem.collectAsState()

    // Track loaded items for navigation
    LaunchedEffect(items.itemCount) {
        loadedItems.clear()
        for (i in 0 until items.itemCount) {
            items[i]?.let { loadedItems.add(it) }
        }
    }

    // Preload adjacent items when navigating near boundaries
    LaunchedEffect(currentItem) {
        currentItem?.let { item ->
            val actualIndex = item.index

            // If near the end, try to load more
            if (actualIndex >= (loadedItems.maxOfOrNull { it.index } ?: 0)) {
                // Peek at next items to trigger paging load
                for (i in actualIndex + 1 until minOf(actualIndex + 6, items.itemCount)) {
                    items[i]?.let { newItem ->
                        if (loadedItems.none { it.id == newItem.id }) {
                            loadedItems.add(newItem)
                        }
                    }
                }
            }

            // If near the beginning, try to load previous
            if (actualIndex <= (loadedItems.minOfOrNull { it.index } ?: Int.MAX_VALUE)) {
                // Peek at previous items to trigger paging load
                for (i in maxOf(actualIndex - 5, 0) until actualIndex) {
                    items[i]?.let { newItem ->
                        if (loadedItems.none { it.id == newItem.id }) {
                            loadedItems.add(newItem)
                        }
                    }
                }
            }
        }
    }

    if (items.itemCount == 0 && items.loadState.refresh is LoadState.NotLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No items found")
        }
    } else {
        LazyVerticalGrid(
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
                                // Sort by index to ensure correct navigation order
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
