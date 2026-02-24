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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.custom_paging.paging.Pager
import com.example.medianav.library.LibraryConstants
import com.example.plugin_common.library.LibraryItem
import com.example.plugin_common.plugin.MediaPlugin

@SuppressLint("MutableCollectionMutableState")
@Composable
internal fun PagingGrid(
    pager: Pager<LibraryItem>,
    plugin: MediaPlugin?,
    onItemClick: (LibraryItem, List<LibraryItem>) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val gridState = rememberLazyGridState()

    // Track previous list size to detect when new items are added
    var previousListSize by remember { mutableStateOf(0) }
    var savedScrollIndex by remember { mutableStateOf(0) }
    var savedScrollOffset by remember { mutableStateOf(0) }

    // Create controller for this pager
    val controller = remember(pager) {
        pager.createController(LibraryConstants.PAGE_SIZE, coroutineScope)
    }

    // Collect state from controller
    val loadedItems by controller.loadedItems.collectAsState()
    val loadedPages by controller.loadedPages.collectAsState()
    val isJumping by controller.isJumping.collectAsState()
    val currentPage by controller.currentPage.collectAsState()
    val totalPages by controller.totalPages.collectAsState()

    // Reset and start when pager changes
    DisposableEffect(pager) {
        controller.reset()

        onDispose {
            // Cleanup handled by controller
        }
    }

    LaunchedEffect(pager) {
        // Reset grid scroll position
        try {
            gridState.scrollToItem(0)
        } catch (_: Throwable) {
            // Ignore if scroll fails
        }

        // Start loading
        controller.start()
    }

    // Handle jumps - scroll to the target page after data loads
    LaunchedEffect(isJumping, loadedPages) {
        if (!isJumping && loadedPages.isNotEmpty()) {
            // After a jump completes, scroll to the page
            val scrollIndex = controller.getScrollIndexForPage(currentPage)
            try {
                gridState.scrollToItem(scrollIndex)
            } catch (_: Throwable) {
                // Ignore if scroll fails
            }
        }
    }

    // Preserve scroll position when new items load
    LaunchedEffect(loadedItems.size) {
        if (loadedItems.size > previousListSize && previousListSize > 0 && !isJumping) {
            // Items were added - restore scroll position
            try {
                gridState.scrollToItem(savedScrollIndex, savedScrollOffset)
            } catch (_: Throwable) {
                // Ignore scroll failures
            }
        }
        previousListSize = loadedItems.size
    }

    // Observe scroll position and update current page / trigger prefetch
    LaunchedEffect(gridState, controller) {
        snapshotFlow {
            val first = gridState.firstVisibleItemIndex
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: first
            val scrollOffset = gridState.firstVisibleItemScrollOffset
            Triple(first, lastVisible, scrollOffset)
        }.collect { (first, lastVisible, scrollOffset) ->
            savedScrollIndex = first
            savedScrollOffset = scrollOffset

            if (!isJumping) {
                controller.updateCurrentPageFromScroll(first, lastVisible)
                controller.prefetchIfNeeded(lastVisible)
            }
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
                count = loadedItems.size,
                key = { index ->
                    val item = loadedItems[index]
                    if (item != null) {
                        "item_${item.id}"
                    } else {
                        val minLoadedPage = loadedPages.minOrNull() ?: 0
                        val absoluteIndex = index + (minLoadedPage * LibraryConstants.PAGE_SIZE)
                        "placeholder_${absoluteIndex}"
                    }
                }
            ) { index ->
                val item = loadedItems[index]
                Box(modifier = Modifier.padding(4.dp)) {
                    if (item != null) {
                        plugin?.let {
                            LibraryCell(
                                item = item,
                                plugin = it,
                                onClick = {
                                    // Pass all non-null loaded items for navigation
                                    val nonNullItems = loadedItems.filterNotNull()
                                    onItemClick(item, nonNullItems)
                                }
                            )
                        }
                    } else {
                        Box(modifier = Modifier)
                    }
                }
            }
        }

        if (totalPages > 1) {
            PageBar(
                currentPage = currentPage,
                totalPages = totalPages,
                onPageChange = { page ->
                    controller.jumpToPage(page)
                }
            )
        }
    }
}
