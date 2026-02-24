package com.example.plugin_anime.jikan

import com.example.plugin_anime.jikan.JikanConverter.toSearchQuery
import com.example.plugin_anime.domain.AnimeByIdParam
import com.example.plugin_anime.domain.AnimeByIdResponse
import com.example.plugin_anime.domain.AnimeGenreQuery
import com.example.plugin_anime.domain.AnimeSearchResponse
import com.example.plugin_anime.domain.Genre
import com.example.plugin_anime.domain.GenreQueryFilter
import com.example.plugin_common.library.LibraryQuery
import com.example.plugin_common.util.mapAsyncNotNull

internal class JikanService {
    private val api = JikanApi()

    suspend fun animeSearch(
        query: LibraryQuery,
        page: Int,
        limit: Int,
        genreCache: Map<GenreQueryFilter, List<Genre>>
    ): Result<AnimeSearchResponse> {
        val query = query.toSearchQuery(page, limit, genreCache)
        return api.getAnimeSearch(query)
    }

    suspend fun getAnimeById(
        id: Int,
        genreCache: Map<GenreQueryFilter, List<Genre>>
    ): Result<AnimeByIdResponse> {
        val param = AnimeByIdParam(id.toString())
        return api.getAnimeById(param)
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