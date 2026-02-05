package com.example.plugin_anime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.plugin_anime.JikanConverter.toLibraryItem
import com.example.plugin_anime.domain.Anime
import com.example.plugin_anime.domain.AnimeSearchQueryOrderBy
import com.example.plugin_anime.domain.AnimeSearchQueryStatus
import com.example.plugin_anime.domain.AnimeSearchQueryType
import com.example.plugin_common.library.LibraryItem
import com.example.plugin_common.library.LibraryQuery
import com.example.plugin_common.library.schema.QuerySchema
import com.example.plugin_common.library.schema.field.BooleanFieldSchema
import com.example.plugin_common.library.schema.field.FilterFieldSchema
import com.example.plugin_common.library.schema.field.SearchFieldSchema
import com.example.plugin_common.library.schema.field.SortFieldSchema
import com.example.plugin_common.plugin.MediaPlugin
import com.example.plugin_common.plugin.info.PluginCategory
import com.example.plugin_common.plugin.info.PluginInfo
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AnimePlugin: MediaPlugin {
    override val info = PluginInfo(
        id = "anime",
        name = "Anime",
        version = "1.0.0",
        category = PluginCategory.VIDEO,
        iconResId = R.drawable.ic_icon
    )

    private val service = AnimeService()
    private val genreCache = runBlocking { service.genreCache() }
    private val animeCache = mutableMapOf<Int, Anime>()
    private val cacheMutex = Mutex()

    override val querySchema = QuerySchema(
        fields = buildMap {
            put(JikanConstants.Query.SEARCH, SearchFieldSchema())
            put(JikanConstants.Query.TYPE, FilterFieldSchema(
                supported = AnimeSearchQueryType.entries.map { it.value }.toSet(),
                include = true
            ))
            put(JikanConstants.Query.STATUS, FilterFieldSchema(
                supported = AnimeSearchQueryStatus.entries.map { it.value }.toSet(),
                include = true
            ))
            put(JikanConstants.Query.SFW, BooleanFieldSchema())

            genreCache.forEach { (filter, genres) ->
                put(filter.value, FilterFieldSchema(
                    supported = genres.map { it.name.lowercase() }.toSet(),
                    multiple = true,
                    include = true,
                    exclude = true
                ))
            }

            put(JikanConstants.Query.ORDER_BY, SortFieldSchema(
                supported = AnimeSearchQueryOrderBy.entries.map { it.value }.toSet(),
                ascending = true,
                descending = true
            ))
        }
    )

    override suspend fun getLibraryItems(
        offset: Int,
        limit: Int,
        query: LibraryQuery
    ): List<LibraryItem> {
        val page = offset / limit + 1
        val result = service.animeSearch(query, page, limit, genreCache)

        val animeList = result.getOrElse { emptyList() }

        cacheMutex.withLock {
            animeList.forEach { anime ->
                animeCache[anime.malId] = anime
            }
        }

        return animeList.map { it.toLibraryItem() }
    }

    override suspend fun getLibraryItemCount(query: LibraryQuery): Int {
        val result = service.animeCount(query, genreCache)
        return result.getOrElse { 0 }
    }

    @Composable
    override fun Thumbnail(item: LibraryItem) {
        var anime by remember { mutableStateOf<Anime?>(null) }

        LaunchedEffect(item.id) {
            cacheMutex.withLock {
                anime = animeCache[item.id.toIntOrNull()]
            }
        }

        AnimeThumbnail(anime)
    }

    @Composable
    override fun DetailsContent(item: LibraryItem) {
        var anime by remember { mutableStateOf<Anime?>(null) }

        LaunchedEffect(item.id) {
            cacheMutex.withLock {
                anime = animeCache[item.id.toIntOrNull()]
            }
        }

        AnimeSummary(anime)
    }

    @Composable
    override fun DropdownContent(item: LibraryItem) {
        var anime by remember { mutableStateOf<Anime?>(null) }

        LaunchedEffect(item.id) {
            cacheMutex.withLock {
                anime = animeCache[item.id.toIntOrNull()]
            }
        }

        AnimeDetails(anime)
    }
}
