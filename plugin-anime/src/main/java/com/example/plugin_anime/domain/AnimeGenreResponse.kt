package com.example.plugin_anime.domain

import com.google.gson.annotations.SerializedName

internal data class AnimeGenreResponse(
    val data: List<Genre>
)

internal data class Genre(
    @SerializedName("mal_id") val malId: Int,
    val name: String,
    val url: String,
    val count: Int
)