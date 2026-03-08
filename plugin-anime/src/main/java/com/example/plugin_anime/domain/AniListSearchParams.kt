package com.example.plugin_anime.domain

import com.example.plugin_anime.anilist.graphql.type.MediaFormat
import com.example.plugin_anime.anilist.graphql.type.MediaSort
import com.example.plugin_anime.anilist.graphql.type.MediaStatus

data class AniListSearchParams(
    val search: String?,
    val format: MediaFormat?,
    val status: MediaStatus?,
    val isAdult: Boolean?,
    val genreIn: List<String>?,
    val genreNotIn: List<String>?,
    val sort: List<MediaSort>?
)
