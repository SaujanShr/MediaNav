package com.example.plugin_anime

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.plugin_anime.JikanConverter.toLibraryItem
import com.example.plugin_anime.domain.Anime
import com.example.plugin_anime.domain.Genre
import com.example.plugin_anime.domain.GenreQueryFilter
import com.example.plugin_common.library.LibraryItem
import com.example.plugin_common.library.LibraryQuery
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class AnimePagingSource(
    private val service: AnimeService,
    private val query: LibraryQuery,
    private val genreCache: Map<GenreQueryFilter, List<Genre>>,
    private val animeCache: MutableMap<Int, Anime>,
    private val cacheMutex: Mutex,
    private val initialPage: Int,
    private val totalCountFlow: MutableStateFlow<Int>,
    private val totalPagesFlow: MutableStateFlow<Int>,
    private val currentPageFlow: MutableStateFlow<Int>
) : PagingSource<Int, LibraryItem>() {

    override fun getRefreshKey(state: PagingState<Int, LibraryItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, LibraryItem> {
        val page = params.key ?: initialPage
        val limit = 10

        return try {
            val result = service.animeSearch(query, page, limit, genreCache)
            val response = result.getOrThrow()
            
            val totalItems = response.pagination.items.total
            totalCountFlow.emit(totalItems)
            totalPagesFlow.emit(response.pagination.lastVisiblePage)
            currentPageFlow.emit(page - 1)

            val items = response.data
            cacheMutex.withLock {
                items.forEach { anime ->
                    animeCache[anime.malId] = anime
                }
            }

            val itemsBefore = (page - 1) * limit
            val itemsAfter = totalItems - itemsBefore - items.size

            LoadResult.Page(
                data = items.map { it.toLibraryItem() },
                prevKey = if (page <= 1) null else page - 1,
                nextKey = if (response.pagination.hasNextPage) page + 1 else null,
                itemsBefore = itemsBefore.coerceAtLeast(0),
                itemsAfter = itemsAfter.coerceAtLeast(0)
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
