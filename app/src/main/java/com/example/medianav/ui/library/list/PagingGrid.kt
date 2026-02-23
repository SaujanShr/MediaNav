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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.custom_paging.paging.Pager
import com.example.medianav.library.LibraryConstants
import com.example.plugin_common.library.LibraryItem
import com.example.plugin_common.plugin.MediaPlugin
import kotlinx.coroutines.launch

@SuppressLint("MutableCollectionMutableState")
@Composable
internal fun PagingGrid(
    pager: Pager<LibraryItem>,
    plugin: MediaPlugin?,
    onScrollPositionChange: (Int, Int) -> Unit,
    onItemClick: (LibraryItem, List<LibraryItem>) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val gridState = rememberLazyGridState()

    // accumulated items and loaded pages (use nullable entries as placeholders)
    val loadedItems = remember { mutableStateListOf<LibraryItem?>() }
    val loadedPages = remember { mutableStateOf(mutableSetOf<Int>()) }
    val loadedIds = remember { mutableStateOf(mutableSetOf<String>()) }
    val isLoading = remember { mutableStateOf(false) }
    val isJumping = remember { mutableStateOf<Int?>(null) } // target page when jumping
    val lastJumpJob = remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }


    // collect pages from pager and append / replace based on jump state
    LaunchedEffect(pager) {
        pager.flow().collect { pagingData ->
            // Use the startIndex from PagingData itself to avoid race conditions
            val startIndex = pagingData.startIndex
            val pageIndex = startIndex / LibraryConstants.PAGE_SIZE

            // Determine the range we need to cover (from min to max loaded pages)
            val minLoadedPage = loadedPages.value.minOrNull() ?: pageIndex
            val maxLoadedPage = loadedPages.value.maxOrNull() ?: pageIndex
            val actualMinPage = minOf(minLoadedPage, pageIndex)
            val actualMaxPage = maxOf(maxLoadedPage, pageIndex)

            val minIndex = actualMinPage * LibraryConstants.PAGE_SIZE
            val maxIndex = (actualMaxPage + 1) * LibraryConstants.PAGE_SIZE

            // Ensure loadedItems covers from minIndex to maxIndex (fill gaps with nulls)
            if (loadedItems.isEmpty()) {
                // First load: start from minIndex
                repeat(maxIndex - minIndex) { loadedItems.add(null) }
            } else {
                // Expand list if needed to cover the new page

                // If the new page is beyond our current range, expand
                while (loadedItems.size < (maxIndex - minIndex)) {
                    loadedItems.add(null)
                }

                // If loading a page before our current minimum, we need to prepend
                if (minIndex < 0) {
                    val itemsToPrepend = -minIndex
                    repeat(itemsToPrepend) {
                        loadedItems.add(0, null)
                    }
                }
            }

            val listBaseIndex = minIndex

            // Insert/replace items at the absolute positions
            pagingData.items.forEach { indexedItem ->
                val absoluteIdx = indexedItem.index
                val relativeIdx = absoluteIdx - listBaseIndex
                val item = indexedItem.value

                if (relativeIdx < 0 || relativeIdx >= loadedItems.size) {
                    // This shouldn't happen, but guard against it
                    return@forEach
                }

                val existing = loadedItems[relativeIdx]
                if (existing == null || existing.id != item.id) {
                    loadedItems[relativeIdx] = item
                }

                loadedIds.value.add(item.id)
            }

            // Track that we've loaded this page
            loadedPages.value.add(pageIndex)

            if (isJumping.value != null) {
                // If we were jumping, scroll to the page start
                val relativeStartIndex = startIndex - listBaseIndex
                try {
                    gridState.scrollToItem(relativeStartIndex.coerceAtLeast(0))
                } catch (_: Throwable) {
                }
                isJumping.value = null
            }

            isLoading.value = false
        }

        // ensure an initial load occurs if nothing has been loaded yet
        if (loadedPages.value.isEmpty()) {
            isLoading.value = true
            coroutineScope.launch {
                try {
                    pager.setCurrentPage(0)
                    pager.fetchPage(0)
                } catch (_: Throwable) {
                    // ignore; pager may already be loading via onSubscription
                }
            }
        }
    }


    // observe scroll position and trigger page load ONLY when user scrolls beyond boundaries
    LaunchedEffect(gridState, pager) {
        snapshotFlow {
            val first = gridState.firstVisibleItemIndex
            val visibleCount = gridState.layoutInfo.visibleItemsInfo.size
            val lastVisibleIndex = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: (first + visibleCount - 1)
            Triple(first, lastVisibleIndex, loadedItems.size)
        }.collect { (first, lastVisible, totalLoaded) ->

            onScrollPositionChange(first, gridState.firstVisibleItemScrollOffset)

            // Calculate the absolute index from the relative position in loadedItems
            val minLoadedPage = loadedPages.value.minOrNull() ?: 0
            val maxLoadedPage = loadedPages.value.maxOrNull()
            val listBaseIndex = minLoadedPage * LibraryConstants.PAGE_SIZE
            val absoluteFirst = first + listBaseIndex

            // Update the current page indicator based on what the user is viewing
            // But NOT while jumping or loading to prevent intermediate page updates
            if (isJumping.value == null && !isLoading.value) {
                // Special case: if scrolled to the end and last page is loaded, show last page
                val totalPages = pager.totalPages.value
                val lastPageIndex = if (totalPages > 0) totalPages - 1 else 0
                val hasLastPage = maxLoadedPage != null && maxLoadedPage == lastPageIndex
                val atEnd = lastVisible >= totalLoaded - 1

                val viewingPage = if (hasLastPage && atEnd) {
                    // User is at the end and we have the last page loaded - show last page
                    lastPageIndex
                } else {
                    // Calculate normally based on scroll position
                    absoluteFirst / LibraryConstants.PAGE_SIZE
                }
                pager.setCurrentPage(viewingPage)
            }

            // Only forward prefetch when user scrolls to the very last item
            if (!isLoading.value && isJumping.value == null && totalLoaded > 0) {
                val maxLoadedPageNum = loadedPages.value.maxOrNull()


                // FORWARD PREFETCH: Only when user scrolls to the very last item
                if (lastVisible >= totalLoaded - 1 && maxLoadedPageNum != null) {
                    val nextPage = maxLoadedPageNum + 1
                    val totalPagesValue = pager.totalPages.value
                    if (!loadedPages.value.contains(nextPage) &&
                        (totalPagesValue !in 1..nextPage)) {
                        isLoading.value = true
                        coroutineScope.launch {
                            pager.fetchPage(nextPage)
                        }
                    }
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        val currentPage by pager.currentPage.collectAsState(initial = 0)
        val totalPages by pager.totalPages.collectAsState(initial = 0)

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            state = gridState,
            contentPadding = PaddingValues(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(
                count = loadedItems.size,
                key = { loadedItems[it]?.id ?: "placeholder_${it}" }
            ) { index ->
                val item = loadedItems[index]
                Box(modifier = Modifier.padding(4.dp)) {
                    if (item != null) {
                        plugin?.let {
                            LibraryCell(
                                item = item,
                                plugin = it,
                                onClick = { onItemClick(item, emptyList()) }
                            )
                        }
                    } else {
                        // Empty placeholder box for unloaded items
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
                    // jump to a page: clear and request page
                    isJumping.value = page
                    // cancel any previous jump and start a new jump job
                    lastJumpJob.value?.cancel()
                    isJumping.value = page
                    // clear current items immediately to avoid mixing pages
                    loadedItems.clear()
                    loadedIds.value.clear()
                    loadedPages.value = mutableSetOf()
                    isLoading.value = true
                    lastJumpJob.value = coroutineScope.launch {
                        try {
                            pager.setCurrentPage(page)
                            pager.fetchPage(page)
                        } catch (_: Throwable) {
                            // ensure we don't stay stuck in loading state if jump fails/cancelled
                            isLoading.value = false
                        }
                    }
                    lastJumpJob.value?.invokeOnCompletion { cause ->
                        // If the job was cancelled or failed (cause != null), reset loading/jump state
                        if (cause != null) {
                            isLoading.value = false
                            isJumping.value = null
                        }
                        // clear the job reference in any case
                        lastJumpJob.value = null
                    }
                }
            )
        }
    }
}

