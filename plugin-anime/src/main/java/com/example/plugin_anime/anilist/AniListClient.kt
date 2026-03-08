package com.example.plugin_anime.anilist

import com.apollographql.apollo.api.Optional
import com.example.plugin_anime.anilist.graphql.GetAnimeQuery
import com.example.plugin_anime.anilist.graphql.GetGenreCollectionQuery
import com.example.plugin_anime.anilist.graphql.SearchAnimeQuery
import com.example.plugin_anime.anilist.graphql.type.MediaFormat
import com.example.plugin_anime.anilist.graphql.type.MediaSort
import com.example.plugin_anime.anilist.graphql.type.MediaStatus
import com.example.plugin_common.api.GraphQLClient

internal class AniListClient {
    private val client = GraphQLClient(
        serverUrl = AniListConstants.Url.BASE_URL,
        requestsPerMinute = AniListConstants.Request.REQUESTS_PER_MINUTE
    )

    suspend fun searchAnime(
        page: Int,
        perPage: Int,
        search: String? = null,
        format: MediaFormat? = null,
        status: MediaStatus? = null,
        genreIn: List<String>? = null,
        genreNotIn: List<String>? = null,
        sort: List<MediaSort>? = null,
        isAdult: Boolean? = null
    ): Result<SearchAnimeQuery.Data> = runCatching {
        val response = client.query(
            SearchAnimeQuery(
                page = page,
                perPage = perPage,
                search = Optional.presentIfNotNull(search),
                format = Optional.presentIfNotNull(format),
                status = Optional.presentIfNotNull(status),
                genre_in = Optional.presentIfNotNull(genreIn),
                genre_not_in = Optional.presentIfNotNull(genreNotIn),
                sort = Optional.presentIfNotNull(sort),
                isAdult = Optional.presentIfNotNull(isAdult)
            )
        ).execute()

        response.data ?: throw Exception("No data in response: ${response.errors}")
    }

    suspend fun getAnimeById(id: Int): Result<AniListMedia> = runCatching {
        val response = client.query(
            GetAnimeQuery(id = id)
        ).execute()

        response.data?.Media ?: throw Exception("No media data in response: ${response.errors}")
    }

    suspend fun getGenreCollection(): Result<List<String>> = runCatching {
        val response = client.query(
            GetGenreCollectionQuery()
        ).execute()

        response.data?.GenreCollection?.filterNotNull() ?: throw Exception("No genre data in response: ${response.errors}")
    }
}
