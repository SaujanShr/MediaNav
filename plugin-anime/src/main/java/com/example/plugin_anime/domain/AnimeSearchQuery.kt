package com.example.plugin_anime.domain

import com.example.plugin_anime.domain.AnimeSearchQueryOrderBy.POPULARITY
import com.example.plugin_anime.domain.AnimeSearchQueryRating.PG_13
import com.example.plugin_anime.domain.AnimeSearchQueryStatus.AIRING
import com.google.gson.annotations.SerializedName

internal data class AnimeSearchQuery(
    val unapproved: Boolean? = null,
    val page: Int? = null,
    val limit: Int? = null,
    val q: String? = null,
    val type: AnimeSearchQueryType? = null,
    val score: Double? = null,

    @SerializedName("min_score")
    val minScore: Double? = null,

    @SerializedName("max_score")
    val maxScore: Double? = null,

    val status: AnimeSearchQueryStatus? = null,
    val rating: AnimeSearchQueryRating? = null,
    val sfw: Boolean? = null,

    val genres: String? = null,

    @SerializedName("genres_exclude")
    val genresExclude: String? = null,

    @SerializedName("order_by")
    val orderBy: AnimeSearchQueryOrderBy? = null,

    val sort: SearchQuerySort? = null,
    val letter: String? = null,

    val producers: String? = null,

    @SerializedName("start_date")
    val startDate: String? = null,

    @SerializedName("end_date")
    val endDate: String? = null
)

internal enum class AnimeSearchQueryType(val value: String) {
    @SerializedName("tv")
    TV("TV"),

    @SerializedName("movie")
    MOVIE("Movie"),

    @SerializedName("ova")
    OVA("OVA"),

    @SerializedName("special")
    SPECIAL("Special"),

    @SerializedName("ona")
    ONA("ONA"),

    @SerializedName("music")
    MUSIC("Music"),

    @SerializedName("cm")
    CM("CM"),

    @SerializedName("pv")
    PV("PV"),

    @SerializedName("tv_special")
    TV_SPECIAL("TV Special");

    companion object {
        private val map = entries.associateBy(AnimeSearchQueryType::value)

        fun fromValue(value: String) = map[value] ?: TV
    }
}

internal enum class AnimeSearchQueryStatus(val value: String) {
    @SerializedName("airing")
    AIRING("Airing"),

    @SerializedName("complete")
    COMPLETE("Complete"),

    @SerializedName("upcoming")
    UPCOMING("Upcoming");

    companion object {
        private val map = entries.associateBy(AnimeSearchQueryStatus::value)

        fun fromValue(value: String) = map[value] ?: AIRING
    }
}

internal enum class AnimeSearchQueryRating(val value: String) {
    @SerializedName("g")
    G("G"),

    @SerializedName("pg")
    PG("PG"),

    @SerializedName("pg13")
    PG_13("PG-13"),

    @SerializedName("r17")
    R_17("R-17"),

    @SerializedName("r")
    R("R"),

    @SerializedName("rx")
    RX("Rx")
}

internal enum class AnimeSearchQueryOrderBy(val value: String) {
    @SerializedName("mal_id")
    MAL_ID("Id"),

    @SerializedName("title")
    TITLE("Title"),

    @SerializedName("start_date")
    START_DATE("Start Date"),

    @SerializedName("end_date")
    END_DATE("End Date"),

    @SerializedName("episodes")
    EPISODES("Episodes"),

    @SerializedName("score")
    SCORE("Score"),

    @SerializedName("scored_by")
    SCORED_BY("Scored By"),

    @SerializedName("rank")
    RANK("Rank"),

    @SerializedName("popularity")
    POPULARITY("Popularity"),

    @SerializedName("members")
    MEMBERS("Members"),

    @SerializedName("favorites")
    FAVORITES("Favorites");

    companion object {
        private val map = entries.associateBy(AnimeSearchQueryOrderBy::value)

        fun fromValue(value: String) = map[value] ?: POPULARITY
    }
}

internal enum class SearchQuerySort(val value: String) {
    @SerializedName("asc")
    ASC("ASC"),

    @SerializedName("desc")
    DESC("DESC")
}

