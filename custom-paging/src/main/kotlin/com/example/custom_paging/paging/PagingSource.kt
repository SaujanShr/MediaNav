package com.example.custom_paging.paging

interface PagingSource<Value : Any> {
    suspend fun load(startIndex: Int): PagingResult<Value>
}
