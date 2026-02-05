package com.example.plugin_anime

import com.example.plugin_anime.JikanConverter.toSearchQuery
import com.example.plugin_anime.domain.*
import com.example.plugin_common.util.mapAsyncNotNull
import com.example.plugin_common.library.LibraryQuery

internal class AnimeService {
    private val api = JikanApi()

    suspend fun animeSearch(
        query: LibraryQuery,
        page: Int,
        limit: Int,
        genreCache: Map<GenreQueryFilter, List<Genre>>
    ): Result<List<Anime>> {
        val query = query.toSearchQuery(page, limit, genreCache)
        val response = api.getAnimeSearch(query)
        return response.map { res -> res.data }
    }

    suspend fun animeCount(
        query: LibraryQuery,
        genreCache: Map<GenreQueryFilter, List<Genre>>
    ): Result<Int> {
        val query = query.toSearchQuery(1, 1, genreCache)
        val response = api.getAnimeSearch(query)
        return response.map { it.pagination.items.total }
    }

    suspend fun genreCache(): Map<GenreQueryFilter, List<Genre>> = runCatching {
        GenreQueryFilter.entries
            .mapAsyncNotNull { filter ->
                api.getAnimeGenres(AnimeGenreQuery(filter))
                    .getOrNull()
                    ?.data
                    ?.let { filter to it }
            }
            .toMap()
    }.getOrElse { emptyMap() }
}