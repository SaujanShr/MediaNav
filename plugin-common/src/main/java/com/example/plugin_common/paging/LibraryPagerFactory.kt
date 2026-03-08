package com.example.plugin_common.paging

import kotlinx.coroutines.CoroutineScope

fun <T : Any, R : Any> createLibraryApiPager(
    pageSize: Int,
    fetch: suspend (page: Int, pageSize: Int) -> Result<R>,
    transform: (result: R, page: Int) -> Pair<List<T>, Int>,
    scope: CoroutineScope
): LibraryPager<T> {
    val fetchPage: suspend (page: Int, pageSize: Int) -> Result<Pair<List<T>, Int>> =
        { page: Int, pageSize: Int ->
            fetch(page, pageSize).fold(
                onSuccess = { result ->
                    val (items, totalCount) = transform(result, page)
                    Result.success(items to totalCount)
                },
                onFailure = { error -> Result.failure(error) }
            )
        }
    val session = LibraryPagingSession(pageSize, fetchPage)

    return LibraryPager(session, scope)
}

fun <T : Any> createLibraryListPager(
    items: List<T>,
    scope: CoroutineScope
): LibraryPager<T> {
    val loadPage: suspend (page: Int, pageSize: Int) -> Result<Pair<List<T>, Int>> =
        { page, pageSize ->
            val startIndex = page * pageSize
            val endIndex = minOf(startIndex + pageSize, items.size)

            if (startIndex >= items.size) {
                Result.success(emptyList<T>() to items.size)
            } else {
                val pageItems = items.subList(startIndex, endIndex)
                Result.success(pageItems to items.size)
            }
        }
    val session = LibraryPagingSession(PagingConstants.Library.PAGE_SIZE, loadPage)

    return LibraryPager(session, scope)
}