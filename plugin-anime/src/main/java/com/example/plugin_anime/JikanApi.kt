package com.example.plugin_anime

import com.example.plugin_anime.domain.Anime
import com.example.plugin_anime.domain.AnimeByIdParam
import com.example.plugin_anime.domain.AnimeByIdResponse
import com.google.gson.Gson
import com.example.plugin_anime.domain.AnimeGenreQuery
import com.example.plugin_anime.domain.AnimeGenreResponse
import com.example.plugin_anime.domain.AnimeSearchQuery
import com.example.plugin_anime.domain.AnimeSearchResponse
import com.example.plugin_common.api.Api
import com.example.plugin_common.util.buildApiUrl

internal class JikanApi {
    private val api = Api(requestsPerMinute = JikanConstants.Request.REQUESTS_PER_MINUTE)
    private val gson = Gson()

    suspend fun getAnimeSearch(query: AnimeSearchQuery): Result<AnimeSearchResponse> {
        val url = buildApiUrl(JikanConstants.Url.ANIME_SEARCH, queryParams = query)

        return api.get(url).mapCatching { responseString ->
            gson.fromJson(responseString, AnimeSearchResponse::class.java)
        }
    }

    suspend fun getAnimeGenres(query: AnimeGenreQuery): Result<AnimeGenreResponse> {
        val url = buildApiUrl(JikanConstants.Url.ANIME_GENRES, pathParams = query)

        return api.get(url).mapCatching { responseString ->
            gson.fromJson(responseString, AnimeGenreResponse::class.java)
        }
    }

    suspend fun getAnimeById(param: AnimeByIdParam): Result<AnimeByIdResponse> {
        val url = buildApiUrl(JikanConstants.Url.ANIME_BY_ID, pathParams = param)

        return api.get(url).mapCatching { responseString ->
            gson.fromJson(responseString, AnimeByIdResponse::class.java)
        }
    }
}
