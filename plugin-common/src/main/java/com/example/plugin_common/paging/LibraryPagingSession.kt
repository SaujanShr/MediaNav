package com.example.plugin_common.paging

import androidx.paging.Pager
import androidx.paging.PagingConfig

class LibraryPagingSession<T: Any>(
    private val loadSize: Int,
    private val loadPage: suspend (Int, Int) -> Result<Pair<List<T>, Int>>
) {
    private val cache = mutableMapOf<Int, Pair<List<T>, Int>>()

    fun createPager(initialPage: Int): Pager<Int, PagerResult<T>> {
        return Pager(
            initialKey = initialPage,
            config = PagingConfig(
                pageSize = PagingConstants.Library.PAGE_SIZE,
                prefetchDistance = PagingConstants.Config.PREFETCH_DISTANCE
            ),
            pagingSourceFactory = { LibraryPagingSource(initialPage, loadSize, loadPage, cache) }
        )
    }
}