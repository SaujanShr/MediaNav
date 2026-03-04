package com.example.plugin_common.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class LibraryPagingSource<T: Any>(
    private val initialPage: Int,
    private val loadSize: Int,
    private val loadPage: suspend (Int, Int) -> Result<Pair<List<T>, Int>>,
    private val cache: MutableMap<Int, Pair<List<T>, Int>>
): PagingSource<Int, PagerResult<T>>() {
     private val cacheMutex = Mutex()

     override fun getRefreshKey(state: PagingState<Int, PagerResult<T>>): Int? {
         return state.anchorPosition?.let { anchorPosition ->
             anchorPosition / PagingConstants.Library.PAGE_SIZE
         }
     }

     override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PagerResult<T>> {
         val page = params.key ?: initialPage
         val startIndex = page * PagingConstants.Library.PAGE_SIZE

         val (allItems, totalCount) = loadItems(startIndex).fold(
             onSuccess = { result -> result },
             onFailure = { error -> return LoadResult.Error(error) }
         )

         val items = getPageItems(startIndex, allItems, totalCount)

         return LoadResult.Page(
             data = items,
             prevKey = if (page > 0) page - 1 else null,
             nextKey = if (startIndex + items.size < totalCount) page + 1 else null
         )
     }

    private suspend fun loadItems(startIndex: Int): Result<Pair<List<T>, Int>> {
        val page = startIndex / loadSize

        return cacheMutex.withLock {
            cache[page]
                ?.let { Result.success(it) }
                ?: loadPage(page, loadSize).onSuccess { cache[page] = it }
        }
    }

    private fun getPageItems(
        startIndex: Int,
        allItems: List<T>,
        totalCount: Int
    ): List<PagerResult<T>> {
        val pageStartIndex = startIndex % loadSize

        return if (pageStartIndex < allItems.size) {
            val pageEndIndex = minOf(
                pageStartIndex + PagingConstants.Library.PAGE_SIZE,
                allItems.size
            )
            allItems
                .subList(pageStartIndex, pageEndIndex)
                .map { PagerResult(data = it, totalCount = totalCount) }
        } else {
            emptyList()
        }
    }
 }