package com.example.plugin_anime

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.plugin_anime.JikanConverter.toLibraryItem
import com.example.plugin_anime.domain.Anime
import com.example.plugin_anime.domain.AnimeSearchQueryOrderBy
import com.example.plugin_anime.domain.AnimeSearchQueryStatus
import com.example.plugin_anime.domain.AnimeSearchQueryType
import com.example.plugin_common.library.LibraryItem
import com.example.plugin_common.library.LibraryQuery
import com.example.plugin_common.library.expression.SortDirection
import com.example.plugin_common.library.schema.QuerySchema
import com.example.plugin_common.library.schema.field.BooleanFieldSchema
import com.example.plugin_common.library.schema.field.FilterFieldSchema
import com.example.plugin_common.library.schema.field.SearchFieldSchema
import com.example.plugin_common.library.schema.field.SortFieldSchema
import com.example.plugin_common.player.PreviewPlayer
import com.example.plugin_common.player.ThumbnailPlayer
import com.example.plugin_common.plugin.MediaPlugin
import com.example.plugin_common.plugin.PluginCategory
import com.example.plugin_common.plugin.PluginMetadata
import com.example.plugin_common.plugin.PluginResources
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import com.example.custom_paging.paging.Pager
import com.example.custom_paging.paging.PagingItem
import com.example.custom_paging.paging.PagingResult
import com.example.custom_paging.paging.createApiPagingSource

class AnimePlugin : MediaPlugin {
    override val metadata = PluginMetadata(
        id = "anime",
        name = "Anime",
        category = PluginCategory.VIDEO
    )

    override val resources = PluginResources(
        iconResId = R.drawable.ic_icon,
        bannerResId = R.drawable.ic_banner
    )

    private val service = AnimeService()
    private val animeCache = mutableMapOf<Int, Anime>()
    private val genreCache = runBlocking { service.genreCache() }
    private val cacheMutex = Mutex()

    private val thumbnailPlayer = ThumbnailPlayer(75f / 106f)
    private val previewPlayer = PreviewPlayer(75f / 106f)

    override suspend fun initialize() {}

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
                descending = true,
                defaultSort = AnimeSearchQueryOrderBy.SCORE.value,
                defaultDirection = SortDirection.DESC
            ))
        }
    )

    override fun getPager(query: LibraryQuery?): Pager<LibraryItem> {
        val finalQuery = query ?: querySchema.defaultQuery()

        return Pager(
            createApiPagingSource(
                fetchSize = JikanConstants.Query.FETCH_SIZE,
                fetch = { page, fetchSize ->
                    service.animeSearch(finalQuery, page, fetchSize, genreCache)
                },
                onSuccess = { response ->
                    cacheMutex.withLock {
                        response.data.forEach { anime -> animeCache[anime.malId] = anime }
                    }
                },
                transform = { response, startIndex ->
                    val items = response.data.mapIndexed { offset, anime ->
                        PagingItem(
                            value = anime.toLibraryItem(startIndex + offset),
                            index = startIndex + offset
                        )
                    }
                    PagingResult.Success(
                        items = items,
                        totalCount = response.pagination.items.total
                    )
                }
            )
        )
    }

    @Composable
    override fun Thumbnail(item: LibraryItem) {
        thumbnailPlayer.Remote(item.thumbnailUrl)
    }

    @Composable
    override fun PreviewContent(item: LibraryItem) {
        val anime = getAnime(item)
        anime?.let {
            previewPlayer.Remote(
                it.images.jpg.largeImageUrl!!,
                listOf()
            )
        }
    }

    @Composable
    override fun SummaryContent(item: LibraryItem) {
        val anime = getAnime(item)
        anime?.let {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = it.titles.firstOrNull { t -> t.type == "Default" }?.title ?: item.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${it.type?.value ?: "Unknown"} • ${it.episodes ?: "?"} eps • ${it.status?.value ?: "Unknown"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                it.score?.let { score ->
                    Text(
                        text = "Score: $score",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    @Composable
    override fun DescriptionContent(item: LibraryItem) {
        val anime = getAnime(item)
        anime?.let {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Synopsis",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = it.synopsis ?: "No synopsis available.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }

    @Composable
    override fun AttributeContent(item: LibraryItem) {
        val anime = getAnime(item)
        anime?.let {
            Column(modifier = Modifier.padding(16.dp)) {
                AttributeRow("Rating", it.rating?.value ?: "Unknown")
                AttributeRow("Source", it.source ?: "Unknown")
                AttributeRow("Aired", it.aired.from ?: "Unknown")
                AttributeRow("Studios", it.studios.joinToString { s -> s.name }.ifEmpty { "Unknown" })
            }
        }
    }

    @Composable
    private fun AttributeRow(label: String, value: String) {
        Row(modifier = Modifier.padding(vertical = 2.dp)) {
            Text(
                text = "$label: ",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }

    @Composable
    override fun PlayableContent(item: LibraryItem) {
    }

    @Composable
    override fun SettingsContent() {
    }

    @Composable
    private fun getAnime(item: LibraryItem): Anime? {
        var anime by remember { mutableStateOf<Anime?>(null) }

        LaunchedEffect(item.id) {
            cacheMutex.withLock {
                anime = animeCache[item.id.toIntOrNull()]
                if (anime == null) {
                    service.getAnimeById(item.id.toInt(), genreCache)
                        .onSuccess { response ->
                            anime = response.data
                            animeCache[item.id.toInt()] = response.data
                        }
                }
            }
        }

        return anime
    }
}
