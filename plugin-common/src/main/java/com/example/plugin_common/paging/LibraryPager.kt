package com.example.plugin_common.paging

import androidx.paging.Pager
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn


class LibraryPager<T : Any>(
    private val session: LibraryPagingSession<T>,
    private val scope: CoroutineScope
) {
    private val initialPage = MutableStateFlow(0)

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    fun setCurrentPage(page: Int) {
        val newPage = page.coerceIn(0, maxOf(0, totalPageCount.value - 1))
        _currentPage.value = newPage
    }

    fun jumpToPage(page: Int) {
        val newPage = page.coerceIn(0, maxOf(0, totalPageCount.value - 1))
        _currentPage.value = newPage
        initialPage.value = newPage
        cachedFlow = null
    }

    private val _totalCount = MutableStateFlow(0)

    val totalPageCount: StateFlow<Int> = _totalCount
        .map { count ->
            (count + PagingConstants.Library.PAGE_SIZE - 1) / PagingConstants.Library.PAGE_SIZE
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    private var cachedFlow: Flow<PagingData<T>>? = null

    fun flow(): Flow<PagingData<T>> {
        if (cachedFlow == null) {
            val pager = session.createPager(initialPage.value)
            cachedFlow = pager.flow.map { pagingData ->
                pagingData.map { pagerResult ->
                    if (_totalCount.value != pagerResult.totalCount) {
                        _totalCount.value = pagerResult.totalCount
                    }
                    pagerResult.data
                }
            }
        }
        return cachedFlow!!
    }
}