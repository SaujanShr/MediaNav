package com.example.custom_paging.paging

interface PagingSource<Value : Any> {
    val loadSize: Int
    suspend fun load(startIndex: Int): PagingResult<Value>
}
