package com.example.custom_paging.paging

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class PagerState<Value : Any>(
    private val source: PagingSource<Value>
) {
    private val refreshMutex = Mutex()
    private val itemCache = mutableMapOf<Int, Value>()

    private val _pages = MutableSharedFlow<PagingData<Value>>(replay = 1)
    val pages: SharedFlow<PagingData<Value>> = _pages.asSharedFlow()

    private val _errors = MutableSharedFlow<Throwable>(replay = 0)
    val errors: SharedFlow<Throwable> = _errors.asSharedFlow()

    private val _totalCount = MutableStateFlow(0)
    val totalCount: StateFlow<Int> = _totalCount.asStateFlow()

    private val _totalPages = MutableStateFlow(0)
    val totalPages: StateFlow<Int> = _totalPages.asStateFlow()

    private val _currentPage = MutableStateFlow(PagingConstants.Page.INITIAL_PAGE)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private fun evictDistantCacheEntries(index: Int) {
        if (itemCache.size > PagingConstants.Cache.MAX_SIZE) {
            val keepWindow = PagingConstants.Cache.KEEP_WINDOW
            val minIndex = maxOf(0, index - keepWindow)
            val maxIndex = index + keepWindow

            itemCache.keys.removeIf { it !in minIndex..maxIndex }
        }
    }

    private suspend fun cacheResult(result: PagingResult<Value>) {
        when (result) {
            is PagingResult.Success -> {
                result.items.forEach { item ->
                    itemCache[item.index] = item.value
                }
                _totalCount.value = result.totalCount
                _totalPages.value = if (result.totalCount > 0) {
                    (result.totalCount + PagingConstants.Page.PAGE_SIZE - 1) / PagingConstants.Page.PAGE_SIZE
                } else {
                    0
                }
            }
            is PagingResult.Error -> {
                _errors.emit(result.error)
            }
        }
    }

    private suspend fun emitPage(startIndex: Int) {
        val emittedItems =
            (0 until PagingConstants.Page.PAGE_SIZE).mapNotNull { offset ->
                val index = startIndex + offset
                itemCache[index]?.let { value ->
                    PagingItem(value, index)
                }
            }
        _pages.emit(PagingData(emittedItems, startIndex))
    }

    private suspend fun load(startIndex: Int, endIndex: Int) {
        val loadSize = source.loadSize
        val firstLoadIndex = (startIndex / loadSize) * loadSize
        val lastLoadIndex = ((endIndex - 1) / loadSize) * loadSize

        var currentLoadIndex = firstLoadIndex
        while (currentLoadIndex <= lastLoadIndex) {
            val chunkFullyCached = (0 until minOf(loadSize, endIndex - currentLoadIndex)).all { offset ->
                itemCache.contains(currentLoadIndex + offset)
            }

            if (!chunkFullyCached) {
                val result = source.load(currentLoadIndex)
                cacheResult(result)
            }

            currentLoadIndex += loadSize
        }
    }

    suspend fun fetchPage(page: Int) {
        refreshMutex.withLock {
            val startIndex = page * PagingConstants.Page.PAGE_SIZE
            val endIndex = startIndex + PagingConstants.Page.PAGE_SIZE

            load(startIndex, endIndex)
            emitPage(startIndex)
            evictDistantCacheEntries(page * PagingConstants.Page.PAGE_SIZE)
        }
    }

    fun setCurrentPage(page: Int) {
        _currentPage.value = page
    }
}