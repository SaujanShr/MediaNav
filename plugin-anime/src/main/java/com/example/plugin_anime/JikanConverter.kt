package com.example.plugin_anime

import com.example.plugin_anime.domain.Anime
import com.example.plugin_anime.domain.AnimeSearchQuery
import com.example.plugin_anime.domain.AnimeSearchQueryOrderBy
import com.example.plugin_anime.domain.AnimeSearchQueryStatus
import com.example.plugin_anime.domain.AnimeSearchQueryType
import com.example.plugin_anime.domain.Genre
import com.example.plugin_anime.domain.GenreQueryFilter
import com.example.plugin_anime.domain.SearchQuerySort
import com.example.plugin_common.library.LibraryItem
import com.example.plugin_common.library.LibraryQuery
import com.example.plugin_common.library.expression.SortDirection
import com.example.plugin_common.util.firstOrDefault

internal object JikanConverter {
    fun LibraryQuery.toSearchQuery(
        page: Int,
        limit: Int,
        genreCache: Map<GenreQueryFilter, List<Genre>>
    ): AnimeSearchQuery {
        val genresInclude = buildGenresInclude(this, genreCache)
        val genresExclude = buildGenresExclude(this, genreCache)

        return AnimeSearchQuery(
            page = page,
            limit = limit,
            q = searchFields["search"],
            type = filterFields["type"]?.include?.firstOrNull()?.let {
                AnimeSearchQueryType.valueOf(it)
            },
            status = filterFields["status"]?.include?.firstOrNull()?.let {
                AnimeSearchQueryStatus.valueOf(it)
            },
            sfw = booleanFields["sfw"],
            genres = genresInclude.joinToString(","),
            genresExclude = genresExclude.joinToString(","),
            orderBy = sortFields["orderBy"]?.sort?.let {
                AnimeSearchQueryOrderBy.valueOf(it)
            },
            sort = sortFields["orderBy"]?.direction?.let {
                when (it) {
                    SortDirection.ASC -> SearchQuerySort.ASC
                    else -> SearchQuerySort.DESC
                }
            }
        )
    }

    fun Anime.toLibraryItem() = LibraryItem(
        id = malId.toString(),
        title = titles.firstOrDefault("Undefined") { title -> title.title },
        thumbnailUrl = images.jpg.largeImageUrl!!
    )

    private fun buildGenresInclude(
        query: LibraryQuery,
        genreCache: Map<GenreQueryFilter, List<Genre>>
    ) = genreCache.entries.flatMap { (filter, genres) ->
        query.filterFields[filter.value]?.include?.mapNotNull { value ->
            genres.find { it.name == value }?.malId?.toString()
        } ?: emptyList()
    }

    private fun buildGenresExclude(
        query: LibraryQuery,
        genreCache: Map<GenreQueryFilter, List<Genre>>
    ) = genreCache.entries.flatMap { (filter, genres) ->
        query.filterFields[filter.value]?.exclude?.mapNotNull { value ->
            genres.find { it.name == value }?.malId?.toString()
        } ?: emptyList()
    }
}