package com.example.plugin_anime.domain

import com.google.gson.annotations.SerializedName

internal data class AnimeSearchResponse(
    val data: List<Anime>,
    val pagination: Pagination
)

internal data class Anime(
    @SerializedName("mal_id")
    val malId: Int,

    val url: String,
    val images: AnimeImages,
    val trailer: TrailerBase,
    val approved: Boolean,
    val titles: List<Title>,
    val type: AnimeType?,
    val source: String?,
    val episodes: Int?,
    val status: AiringStatus?,
    val airing: Boolean,
    val aired: DateRange,
    val duration: String?,
    val rating: AnimeAudienceRating?,
    val score: Double?,

    @SerializedName("scored_by")
    val scoredBy: Int?,

    val rank: Int?,
    val popularity: Int?,
    val members: Int?,
    val favorites: Int?,
    val synopsis: String?,
    val background: String?,
    val season: Season?,
    val year: Int?,
    val broadcast: Broadcast,
    val producers: List<MalUrl>,
    val licensors: List<MalUrl>,
    val studios: List<MalUrl>,
    val genres: List<MalUrl>,

    @SerializedName("explicit_genres")
    val explicitGenres: List<MalUrl>,

    val themes: List<MalUrl>,
    val demographics: List<MalUrl>
)

internal data class AnimeImages(
    val jpg: ImageUrls,
    val webp: ImageUrls
)

internal data class ImageUrls(
    @SerializedName("image_url")
    val imageUrl: String?,

    @SerializedName("small_image_url")
    val smallImageUrl: String?,

    @SerializedName("large_image_url")
    val largeImageUrl: String?
)

internal data class TrailerBase(
    @SerializedName("youtube_id")
    val youtubeId: String?,

    val url: String?,

    @SerializedName("embed_url")
    val embedUrl: String?
)

internal data class Title(
    val type: String,
    val title: String
)

internal data class DateRange(
    val from: String?,
    val to: String?,
    val prop: Prop
)

internal data class Prop(
    val from: DateProp,
    val to: DateProp,
    val string: String?
)

internal data class DateProp(
    val day: Int?,
    val month: Int?,
    val year: Int?
)

internal data class Broadcast(
    val day: String?,
    val time: String?,
    val timezone: String?,
    val string: String?
)

internal data class MalUrl(
    @SerializedName("mal_id")
    val malId: Int,

    val type: String,
    val name: String,
    val url: String
)

internal data class Pagination(
    @SerializedName("last_visible_page")
    val lastVisiblePage: Int,

    @SerializedName("has_next_page")
    val hasNextPage: Boolean,

    @SerializedName("current_page")
    val currentPage: Int,

    val items: Items
)

internal data class Items(
    val count: Int,
    val total: Int,

    @SerializedName("per_page")
    val perPage: Int
)

internal enum class AnimeType(val value: String) {
    @SerializedName("TV")
    TV("TV"),

    @SerializedName("Movie")
    MOVIE("Movie"),

    @SerializedName("OVA")
    OVA("OVA"),

    @SerializedName("Special")
    SPECIAL("Special"),

    @SerializedName("ONA")
    ONA("ONA"),

    @SerializedName("Music")
    MUSIC("Music")
}

internal enum class AiringStatus(val value: String) {
    @SerializedName("Finished Airing")
    FINISHED_AIRING("Finished Airing"),

    @SerializedName("Currently Airing")
    CURRENTLY_AIRING("Currently Airing"),

    @SerializedName("Not yet aired")
    NOT_YET_AIRED("Not Yet Aired")
}

internal enum class AnimeAudienceRating(val value: String) {
    @SerializedName("G - All Ages")
    G_ALL_AGES("G"),

    @SerializedName("PG - Children")
    PG_CHILDREN("PG"),

    @SerializedName("PG-13 - Teens 13 or older")
    PG_13_TEENS_13_OR_OLDER("PG-13"),

    @SerializedName("R - 17+ (violence & profanity)")
    R_18_VIOLENCE_PROFANITY("R"),

    @SerializedName("R+ - Mild Nudity")
    R_PLUS_MILD_NUDITY("R+"),

    @SerializedName("Rx - Hentai")
    RX_HENTAI("Rx")
}

internal enum class Season(val value: String) {
    @SerializedName("winter")
    WINTER("Winter"),

    @SerializedName("spring")
    SPRING("Spring"),

    @SerializedName("summer")
    SUMMER("Summer"),

    @SerializedName("fall")
    FALL("Fall")
}