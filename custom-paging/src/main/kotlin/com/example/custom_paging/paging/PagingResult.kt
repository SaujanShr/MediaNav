package com.example.custom_paging.paging

sealed class PagingResult<out Value : Any> {
    data class Success<Value : Any>(
        val items: List<PagingItem<Value>>,
        val totalCount: Int
    ) : PagingResult<Value>()

    data class Error(val error: Throwable) : PagingResult<Nothing>()
}
