package com.example.custom_paging.paging

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PagerController<Value : Any>(
    private val pager: Pager<Value>,
    private val pageSize: Int,
    private val scope: CoroutineScope
) {
    private val _loadedItems = MutableStateFlow<List<Value?>>(emptyList())
    val loadedItems: StateFlow<List<Value?>> = _loadedItems.asStateFlow()

    private val _loadedPages = MutableStateFlow<Set<Int>>(emptySet())
    val loadedPages: StateFlow<Set<Int>> = _loadedPages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isJumping = MutableStateFlow(false)
    val isJumping: StateFlow<Boolean> = _isJumping.asStateFlow()

    private var jumpJob: Job? = null

    val currentPage: StateFlow<Int> = pager.currentPage
    val totalPages: StateFlow<Int> = pager.totalPages

    init {
        // Start collecting pages
        scope.launch {
            pager.flow().collect { pagingData ->
                handlePageData(pagingData)
            }
        }
    }

    suspend fun start() {
        _isLoading.value = true
        pager.setCurrentPage(PagingConstants.Page.INITIAL_PAGE)
        pager.fetchPage(PagingConstants.Page.INITIAL_PAGE)
    }

    fun reset() {
        _loadedItems.value = emptyList()
        _loadedPages.value = emptySet()
        _isLoading.value = false
        _isJumping.value = false
        jumpJob?.cancel()
        jumpJob = null
    }

    fun updateCurrentPageFromScroll(firstVisibleIndex: Int, lastVisibleIndex: Int) {
        if (_isJumping.value || _isLoading.value) return

        val loadedPagesList = _loadedPages.value.toList()
        if (loadedPagesList.isEmpty()) return

        val minLoadedPage = loadedPagesList.minOrNull() ?: 0
        val maxLoadedPage = loadedPagesList.maxOrNull() ?: 0
        val listBaseIndex = minLoadedPage * pageSize
        val absoluteFirst = firstVisibleIndex + listBaseIndex
        val totalLoaded = _loadedItems.value.size

        // Special case: if scrolled to the end and last page is loaded, show last page
        val totalPagesValue = pager.totalPages.value
        val lastPageIndex = if (totalPagesValue > 0) totalPagesValue - 1 else 0
        val hasLastPage = maxLoadedPage == lastPageIndex
        val atEnd = lastVisibleIndex >= totalLoaded - 1

        val viewingPage = if (hasLastPage && atEnd) {
            lastPageIndex
        } else {
            absoluteFirst / pageSize
        }

        pager.setCurrentPage(viewingPage)
    }

    fun prefetchIfNeeded(lastVisibleIndex: Int) {
        if (_isLoading.value || _isJumping.value) return

        val totalLoaded = _loadedItems.value.size
        if (totalLoaded == 0) return

        val maxLoadedPage = _loadedPages.value.maxOrNull() ?: return

        // FORWARD PREFETCH: Only when user scrolls to the very last item
        if (lastVisibleIndex >= totalLoaded - 1) {
            val nextPage = maxLoadedPage + 1
            val totalPagesValue = pager.totalPages.value

            if (!_loadedPages.value.contains(nextPage) &&
                (totalPagesValue !in 1..nextPage)) {
                _isLoading.value = true
                scope.launch {
                    pager.fetchPage(nextPage)
                }
            }
        }
    }

    fun jumpToPage(page: Int): Job {
        jumpJob?.cancel()
        _isJumping.value = true
        _loadedItems.value = emptyList()
        _loadedPages.value = emptySet()
        _isLoading.value = true

        val job = scope.launch {
            try {
                pager.setCurrentPage(page)
                pager.fetchPage(page)
            } catch (_: Throwable) {
                _isLoading.value = false
                _isJumping.value = false
            }
        }

        job.invokeOnCompletion { cause ->
            if (cause != null) {
                _isLoading.value = false
                _isJumping.value = false
            }
            jumpJob = null
        }

        jumpJob = job
        return job
    }

    private fun handlePageData(pagingData: PagingData<Value>) {
        val startIndex = pagingData.startIndex
        val pageIndex = startIndex / pageSize

        val currentLoadedPages = _loadedPages.value
        val currentItems = _loadedItems.value

        // Determine the range we need to cover (from min to max loaded pages)
        val minLoadedPage = currentLoadedPages.minOrNull() ?: pageIndex
        val maxLoadedPage = currentLoadedPages.maxOrNull() ?: pageIndex
        val actualMinPage = minOf(minLoadedPage, pageIndex)
        val actualMaxPage = maxOf(maxLoadedPage, pageIndex)

        val minIndex = actualMinPage * pageSize
        val maxIndex = (actualMaxPage + 1) * pageSize
        val requiredSize = maxIndex - minIndex

        val newItems = if (currentItems.isEmpty()) {
            // First load: create initial list with nulls
            MutableList<Value?>(requiredSize) { null }
        } else {
            // Create a new list maintaining existing items
            val newList = currentItems.toMutableList()

            // If we need to expand forward, add more nulls at the end
            while (newList.size < requiredSize) {
                newList.add(null)
            }

            newList
        }

        // Calculate the list base index
        val listBaseIndex = minIndex

        // Insert items at their proper positions
        pagingData.items.forEach { indexedItem ->
            val absoluteIdx = indexedItem.index
            val relativeIdx = absoluteIdx - listBaseIndex
            val item = indexedItem.value

            if (relativeIdx >= 0 && relativeIdx < newItems.size) {
                newItems[relativeIdx] = item
            }
        }

        // Update state
        _loadedItems.value = newItems
        _loadedPages.value = currentLoadedPages + pageIndex
        _isLoading.value = false

        if (_isJumping.value) {
            _isJumping.value = false
        }
    }

    fun getScrollIndexForPage(page: Int): Int {
        val loadedPagesList = _loadedPages.value.toList()
        if (loadedPagesList.isEmpty()) return 0

        val minLoadedPage = loadedPagesList.minOrNull() ?: 0
        val startIndex = page * pageSize
        val listBaseIndex = minLoadedPage * pageSize
        val relativeStartIndex = startIndex - listBaseIndex
        return relativeStartIndex.coerceAtLeast(0)
    }
}

