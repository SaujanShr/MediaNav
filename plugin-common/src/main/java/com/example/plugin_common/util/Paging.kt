package com.example.plugin_common.util

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.example.plugin_common.library.LibraryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

fun createLibraryPager(
    pageSize: Int = 10,
    pagingSourceFactory: (
        initialPage: Int,
        totalCountFlow: MutableStateFlow<Int>,
        totalPagesFlow: MutableStateFlow<Int>,
        currentPageFlow: MutableStateFlow<Int>
    ) -> PagingSource<Int, LibraryItem>
): Flow<PagingData<LibraryItem>> {
    val totalCountFlow = MutableStateFlow(0)
    val totalPagesFlow = MutableStateFlow(0)
    val currentPageFlow = MutableStateFlow(0)
    
    var currentPagingSource: PagingSource<Int, LibraryItem>? = null
    var nextInitialPage: Int? = null

    val pager = Pager(
        config = PagingConfig(
            pageSize = pageSize,
            enablePlaceholders = true
        ),
        pagingSourceFactory = {
            val startPage = nextInitialPage ?: 1
            nextInitialPage = null
            
            pagingSourceFactory(
                startPage,
                totalCountFlow,
                totalPagesFlow,
                currentPageFlow
            ).also { currentPagingSource = it }
        }
    )

    return pager.flow
}