package com.example.plugin_common.paging

data class PagerResult<T : Any>(
    val data: T,
    val totalCount: Int
)

