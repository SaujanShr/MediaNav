package com.example.custom_paging.paging

fun <Value : Any> createPagingSource(
    loadSize: Int,
    loader: suspend (startIndex: Int) -> PagingResult<Value>
): PagingSource<Value> {
    return object : PagingSource<Value> {
        override val loadSize: Int = loadSize
        override suspend fun load(startIndex: Int): PagingResult<Value> {
            return loader(startIndex)
        }
    }
}

fun <T : Any, Value : Any> createApiPagingSource(
    fetchSize: Int,
    fetch: suspend (page: Int, fetchSize: Int) -> Result<T>,
    onSuccess: (suspend (T) -> Unit)? = null,
    transform: (T, startIndex: Int) -> PagingResult<Value>
): PagingSource<Value> {
    return createPagingSource(loadSize = fetchSize) { startIndex ->
        val page = (startIndex / fetchSize) + 1
        val apiPageStartIndex = (page - 1) * fetchSize
        val result = fetch(page, fetchSize)

        result.fold(
            onSuccess = { response ->
                onSuccess?.invoke(response)
                transform(response, apiPageStartIndex)
            },
            onFailure = { error ->
                PagingResult.Error(error)
            }
        )
    }
}

