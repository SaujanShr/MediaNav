package com.example.plugin_common.paging

/**
 * Wrapper class that contains both the data and metadata about total pages
 */
data class PagerResult<T : Any>(
    val data: T,
    val totalPageCount: Int
)

