package com.example.plugin_common.paging

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map


class LibraryPager<T : Any>(private val pager: Pager<Int, PagerResult<T>>) {
    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    fun setCurrentPage(page: Int) {
        _currentPage.value = page.coerceIn(0, maxOf(0, _totalPageCount.value - 1))
    }

    private val _totalPageCount = MutableStateFlow(0)
    val totalPageCount: StateFlow<Int> = _totalPageCount.asStateFlow()

    private var cachedFlow: Flow<PagingData<T>>? = null

    fun flow(): Flow<PagingData<T>> {
        if (cachedFlow == null) {
            cachedFlow = pager.flow.map { pagingData ->
                pagingData.map { pagerResult ->
                    if (_totalPageCount.value != pagerResult.totalPageCount) {
                        _totalPageCount.value = pagerResult.totalPageCount
                    }
                    pagerResult.data
                }
            }
        }
        return cachedFlow!!
    }
}

fun <T : Any> createLibraryListPager(
    items: List<T>,
    pageSize: Int = 25,
    prefetchDistance: Int = 5
): LibraryPager<T> {
    val totalPages = if (items.isEmpty()) 0 else (items.size + pageSize - 1) / pageSize

    val pager = Pager(
        config = PagingConfig(
            pageSize = pageSize,
            prefetchDistance = prefetchDistance,
            enablePlaceholders = false
        ),
        pagingSourceFactory = {
            object : PagingSource<Int, PagerResult<T>>() {
                override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PagerResult<T>> {
                    return try {
                        val page = params.key ?: 0
                        val startIndex = page * pageSize
                        val endIndex = minOf(startIndex + pageSize, items.size)

                        if (startIndex >= items.size) {
                            LoadResult.Page(
                                data = emptyList(),
                                prevKey = if (page > 0) page - 1 else null,
                                nextKey = null
                            )
                        } else {
                            val pageItems = items.subList(startIndex, endIndex).map { item ->
                                PagerResult(data = item, totalPageCount = totalPages)
                            }
                            LoadResult.Page(
                                data = pageItems,
                                prevKey = if (page > 0) page - 1 else null,
                                nextKey = if (endIndex < items.size) page + 1 else null
                            )
                        }
                    } catch (e: Exception) {
                        LoadResult.Error(e)
                    }
                }

                override fun getRefreshKey(state: PagingState<Int, PagerResult<T>>): Int? {
                    return state.anchorPosition?.let { anchorPosition ->
                        val anchorPage = state.closestPageToPosition(anchorPosition)
                        anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
                    }
                }
            }
        }
    )

    return LibraryPager(pager)
}

fun <T : Any, R : Any> createLibraryApiPager(
    pageSize: Int = 25,
    prefetchDistance: Int = 5,
    fetch: suspend (page: Int, pageSize: Int) -> Result<R>,
    transform: (R, page: Int) -> Pair<List<T>, Int>
): LibraryPager<T> {
    val pager = Pager(
        config = PagingConfig(
            pageSize = pageSize,
            prefetchDistance = prefetchDistance,
            enablePlaceholders = false
        ),
        pagingSourceFactory = {
            object : PagingSource<Int, PagerResult<T>>() {
                override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PagerResult<T>> {
                    val page = params.key ?: 0
                    val result = fetch(page, pageSize)

                    return result.fold(
                        onSuccess = { response ->
                            val (items, totalCount) = transform(response, page)
                            val totalPages = if (totalCount == 0) 0 else (totalCount + pageSize - 1) / pageSize

                            val wrappedItems = items.map { item ->
                                PagerResult(data = item, totalPageCount = totalPages)
                            }

                            LoadResult.Page(
                                data = wrappedItems,
                                prevKey = if (page > 1) page - 1 else null,
                                nextKey = if (items.isNotEmpty() && page < totalPages) page + 1 else null
                            )
                        },
                        onFailure = { error ->
                            LoadResult.Error(error)
                        }
                    )
                }

                override fun getRefreshKey(state: PagingState<Int, PagerResult<T>>): Int? {
                    return state.anchorPosition?.let { anchorPosition ->
                        val anchorPage = state.closestPageToPosition(anchorPosition)
                        anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
                    }
                }
            }
        }
    )

    return LibraryPager(pager)
}

