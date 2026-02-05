package com.example.plugin_anime.domain

import com.google.gson.annotations.SerializedName

internal data class AnimeGenreQuery(
    val filter: GenreQueryFilter?
)

internal enum class GenreQueryFilter(val value: String) {
    @SerializedName("genres")
    GENRES("Genres"),

    @SerializedName("explicit_genres")
    EXPLICIT_GENRES("Explicit Genres"),

    @SerializedName("themes")
    THEMES("Themes"),

    @SerializedName("demographics")
    DEMOGRAPHICS("Demographics")
}