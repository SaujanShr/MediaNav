package com.example.plugin_anime.anilist

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.apollographql.apollo.network.okHttpClient
import com.example.plugin_anime.anilist.graphql.GetAnimeQuery
import com.example.plugin_anime.anilist.graphql.GetGenreCollectionQuery
import com.example.plugin_anime.anilist.graphql.SearchAnimeQuery
import com.example.plugin_anime.anilist.graphql.type.MediaFormat
import com.example.plugin_anime.anilist.graphql.type.MediaSort
import com.example.plugin_anime.anilist.graphql.type.MediaStatus
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

internal class AniListClient {
    private val rateLimitInterceptor = RateLimitInterceptor(
        requestsPerMinute = AniListConstants.Request.REQUESTS_PER_MINUTE
    )

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(rateLimitInterceptor)
        .build()

    private val apolloClient = ApolloClient.Builder()
        .serverUrl(AniListConstants.Url.BASE_URL)
        .okHttpClient(okHttpClient)
        .build()

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
        val response = apolloClient.query(
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
        val response = apolloClient.query(
            GetAnimeQuery(id = id)
        ).execute()

        response.data?.Media ?: throw Exception("No media data in response: ${response.errors}")
    }

    suspend fun getGenreCollection(): Result<List<String>> = runCatching {
        val response = apolloClient.query(
            GetGenreCollectionQuery()
        ).execute()

        response.data?.GenreCollection?.filterNotNull() ?: throw Exception("No genre data in response: ${response.errors}")
    }
}

private class RateLimitInterceptor(private val requestsPerMinute: Int) : Interceptor {
    private val mutex = Mutex()
    private var nextRequestTime = 0L
    private val minIntervalMs = (60_000.0 / requestsPerMinute).toLong()

    override fun intercept(chain: Interceptor.Chain): Response {
        // Use runBlocking to handle suspend function in interceptor
        kotlinx.coroutines.runBlocking {
            mutex.withLock {
                val now = System.currentTimeMillis()
                val waitTime = nextRequestTime - now
                if (waitTime > 0) {
                    kotlinx.coroutines.delay(waitTime)
                }
                nextRequestTime = System.currentTimeMillis() + minIntervalMs
            }
        }
        return chain.proceed(chain.request())
    }
}
