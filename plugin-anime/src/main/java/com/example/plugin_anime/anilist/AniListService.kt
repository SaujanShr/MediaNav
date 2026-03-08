package com.example.plugin_anime.anilist

import com.example.plugin_anime.anilist.AniListConverter.toAnime
import com.example.plugin_anime.anilist.AniListConverter.toSearchParams
import com.example.plugin_anime.domain.Anime
import com.example.plugin_common.library.LibraryQuery
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class AniListService {
    private val client = AniListClient()

    private val animeCache = mutableMapOf<Int, Anime>()
    private val cacheMutex = Mutex()

    suspend fun animeSearch(
        query: LibraryQuery,
        page: Int,
        pageSize: Int
    ): Result<Pair<List<Anime>, Int>> {
        val params = query.toSearchParams()

        val response = client.searchAnime(
            page = page,
            perPage = pageSize,
            search = params.search,
            format = params.format,
            status = params.status,
            genreIn = params.genreIn,
            genreNotIn = params.genreNotIn,
            sort = params.sort,
            isAdult = params.isAdult
        )

        return response.map { data ->
            val animeList = data.Page?.media?.mapNotNull { it?.toAnime() } ?: emptyList()
            val total = data.Page?.pageInfo?.total ?: 0

            cacheMutex.withLock {
                animeList.forEach { anime -> animeCache[anime.id] = anime }
            }

            animeList to total
        }
    }

    suspend fun getAnimeById(id: Int): Result<Anime> {
        cacheMutex.withLock {
            animeCache[id]?.let { return Result.success(it) }
        }

        return client
            .getAnimeById(id)
            .map {
                val anime = it.toAnime()
                cacheMutex.withLock { animeCache[anime.id] = anime }
                anime
            }
    }

    suspend fun getGenreCollection(): List<String> = client
        .getGenreCollection()
        .getOrNull() ?: emptyList()
}

