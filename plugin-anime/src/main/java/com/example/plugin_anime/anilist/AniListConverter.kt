package com.example.plugin_anime.anilist

import com.example.plugin_anime.anilist.graphql.GetAnimeQuery
import com.example.plugin_anime.anilist.graphql.SearchAnimeQuery
import com.example.plugin_anime.anilist.graphql.type.MediaFormat
import com.example.plugin_anime.anilist.graphql.type.MediaSort
import com.example.plugin_anime.anilist.graphql.type.MediaStatus
import com.example.plugin_anime.domain.AniListSearchParams
import com.example.plugin_anime.domain.Anime
import com.example.plugin_anime.domain.AnimeCoverImage
import com.example.plugin_anime.domain.AnimeTitle
import com.example.plugin_common.library.LibraryItem
import com.example.plugin_common.library.LibraryQuery

typealias AniListMedia = GetAnimeQuery.Media
typealias AniListSearchMedia = SearchAnimeQuery.Medium

internal object AniListConverter {
    fun LibraryQuery.toSearchParams(): AniListSearchParams {
        val genresInclude = filterFields["genres"]?.include?.toList() ?: emptyList()
        val genresExclude = filterFields["genres"]?.exclude?.toList() ?: emptyList()

        return AniListSearchParams(
            search = searchFields["search"],
            format = filterFields["format"]?.include?.firstOrNull()?.let {
                MediaFormat.knownEntries.find { format -> format.rawValue == it }
            },
            status = filterFields["status"]?.include?.firstOrNull()?.let {
                MediaStatus.knownEntries.find { status -> status.rawValue == it }
            },
            isAdult = booleanFields["sfw"]?.let { !it },
            genreIn = genresInclude.ifEmpty { null },
            genreNotIn = genresExclude.ifEmpty { null },
            sort = sortFields["orderBy"]?.let { field ->
                val sortValue = field.sort
                val sortEnum = MediaSort.knownEntries.find { it.rawValue == sortValue }
                sortEnum?.let { listOf(it) }
            }
        )
    }

    fun AniListMedia.toAnime() = Anime(
        id = id,
        title = AnimeTitle(
            romaji = title?.romaji,
            english = title?.english,
            native = title?.native
        ),
        coverImage = AnimeCoverImage(
            large = coverImage?.large,
            medium = coverImage?.medium
        ),
        bannerImage = bannerImage,
        format = format,
        status = status,
        episodes = episodes,
        duration = duration,
        season = season?.rawValue,
        seasonYear = seasonYear,
        averageScore = averageScore,
        popularity = popularity,
        favourites = favourites,
        description = description,
        genres = genres?.filterNotNull() ?: emptyList(),
        studios = studios?.edges?.mapNotNull { it?.node?.name } ?: emptyList(),
        source = source?.rawValue,
        isAdult = isAdult ?: false
    )

    fun AniListSearchMedia.toAnime() = Anime(
        id = id,
        title = AnimeTitle(
            romaji = title?.romaji,
            english = title?.english,
            native = title?.native
        ),
        coverImage = AnimeCoverImage(
            large = coverImage?.large,
            medium = coverImage?.medium
        ),
        bannerImage = bannerImage,
        format = format,
        status = status,
        episodes = episodes,
        duration = duration,
        season = season?.rawValue,
        seasonYear = seasonYear,
        averageScore = averageScore,
        popularity = popularity,
        favourites = favourites,
        description = description,
        genres = genres?.filterNotNull() ?: emptyList(),
        studios = studios?.nodes?.mapNotNull { it?.name } ?: emptyList(),
        source = source?.rawValue,
        isAdult = isAdult ?: false
    )

    fun Anime.toLibraryItem(index: Int) = LibraryItem(
        id = id.toString(),
        title = title.romaji ?: title.native ?: "Unknown",
        thumbnailUrl = coverImage.large ?: coverImage.medium ?: "",
        index = index
    )
}
