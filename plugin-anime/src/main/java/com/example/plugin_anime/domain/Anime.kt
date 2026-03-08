package com.example.plugin_anime.domain

import com.example.plugin_anime.anilist.graphql.type.MediaFormat
import com.example.plugin_anime.anilist.graphql.type.MediaStatus

data class Anime(
    val id: Int,
    val title: AnimeTitle,
    val coverImage: AnimeCoverImage,
    val bannerImage: String?,
    val format: MediaFormat?,
    val status: MediaStatus?,
    val episodes: Int?,
    val duration: Int?,
    val season: String?,
    val seasonYear: Int?,
    val averageScore: Int?,
    val popularity: Int?,
    val favourites: Int?,
    val description: String?,
    val genres: List<String>,
    val studios: List<String>,
    val source: String?,
    val isAdult: Boolean
)

data class AnimeTitle(
    val romaji: String?,
    val english: String?,
    val native: String?
)

data class AnimeCoverImage(
    val large: String?,
    val medium: String?
)