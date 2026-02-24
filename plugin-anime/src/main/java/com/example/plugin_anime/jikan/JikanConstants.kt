package com.example.plugin_anime.jikan

internal object JikanConstants {
    object Request {
        const val REQUESTS_PER_MINUTE = 60
    }

    object Url {
        const val BASE_URL = "https://api.jikan.moe/v4"
        const val ANIME_SEARCH = "$BASE_URL/anime"
        const val ANIME_GENRES = "$BASE_URL/genres/anime"
        const val ANIME_BY_ID = "$BASE_URL/anime/{id}"

    }

    object Query {
        const val FETCH_SIZE = 25
        const val SEARCH = "search"
        const val TYPE = "type"
        const val STATUS = "status"
        const val SFW = "sfw"
        const val ORDER_BY = "orderBy"
    }
}