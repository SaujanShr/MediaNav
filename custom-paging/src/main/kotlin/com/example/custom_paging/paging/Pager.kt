package com.example.custom_paging.paging

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class Pager<Value : Any>(pagingSource: PagingSource<Value>) {
    private lateinit var scope: CoroutineScope
    private val state = PagerState(pagingSource)


    val currentPage: StateFlow<Int> = state.currentPage
    val totalPages: StateFlow<Int> = state.totalPages


    fun initialize(scope: CoroutineScope) {
        this.scope = scope
    }

    fun flow(): Flow<PagingData<Value>> = state.pages

    suspend fun fetchPage(page: Int) {
        state.fetchPage(page)
    }

    fun setCurrentPage(page: Int) {
        state.setCurrentPage(page)
    }

    fun createController(pageSize: Int, scope: CoroutineScope): PagerController<Value> {
        return PagerController(this, pageSize, scope)
    }
}
