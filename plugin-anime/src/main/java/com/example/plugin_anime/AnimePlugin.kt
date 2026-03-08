package com.example.plugin_anime

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.plugin_anime.anilist.AniListConverter.toLibraryItem
import com.example.plugin_common.library.LibraryItem
import com.example.plugin_common.library.LibraryQuery
import com.example.plugin_common.library.expression.SortDirection
import com.example.plugin_common.library.schema.QuerySchema
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
import com.example.plugin_common.paging.LibraryPager
import com.example.plugin_common.paging.createLibraryApiPager
import com.example.plugin_anime.anilist.AniListConstants
import com.example.plugin_anime.anilist.AniListService
import com.example.plugin_anime.anilist.graphql.type.MediaFormat
import com.example.plugin_anime.anilist.graphql.type.MediaSort
import com.example.plugin_anime.anilist.graphql.type.MediaStatus
import com.example.plugin_anime.domain.Anime
import com.example.plugin_anime.ui.AnimeSettingsScreen
import com.example.plugin_common.library.schema.field.DateFieldSchema
import kotlinx.coroutines.CoroutineScope

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

    private val service = AniListService()
    private val genreCache = runBlocking { service.getGenreCollection() }

    private val thumbnailPlayer = ThumbnailPlayer(75f / 106f)
    private val previewPlayer = PreviewPlayer(75f / 106f)

    override suspend fun initialize() {}

    override val querySchema = QuerySchema(
        fields = buildMap {
            put("search", SearchFieldSchema())
            put(
                "format", FilterFieldSchema(
                supported = MediaFormat.knownEntries.map { it.rawValue }.toSet(),
                include = true
            ))
            put(
                "status", FilterFieldSchema(
                supported = MediaStatus.knownEntries.map { it.rawValue }.toSet(),
                include = true
            ))
            put("date", DateFieldSchema(range = true))
            put("genres", FilterFieldSchema(
                supported = genreCache.toSet(),
                multiple = true,
                include = true,
                exclude = true
            ))
            put(
                "orderBy", SortFieldSchema(
                supported = MediaSort.knownEntries.map { it.rawValue }.toSet(),
                ascending = true,
                descending = true,
                defaultSort = MediaSort.SCORE_DESC.rawValue,
                defaultDirection = SortDirection.DESC
            ))
        }
    )

    override fun getPager(query: LibraryQuery?, scope: CoroutineScope): LibraryPager<LibraryItem> {
        val finalQuery = query ?: querySchema.defaultQuery()

        return createLibraryApiPager(
            pageSize = AniListConstants.Query.FETCH_SIZE,
            fetch = { page, fetchSize ->
                service.animeSearch(finalQuery, page+1, fetchSize)
            },
            transform = { (items, total), page ->
                val startIndex = page * AniListConstants.Query.FETCH_SIZE
                items.mapIndexed { index, anime ->
                    anime.toLibraryItem(startIndex + index)
                } to total
            },
            scope = scope
        )
    }

    @Composable
    override fun Thumbnail(item: LibraryItem) {
        thumbnailPlayer.Remote(item.thumbnailUrl)
    }

    @Composable
    override fun PreviewContent(item: LibraryItem) {
        val anime = runBlocking { getAnime(item) }
        anime?.let {
            previewPlayer.Remote(
                it.coverImage.large ?: it.coverImage?.medium ?: "",
                listOf()
            )
        }
    }

    @Composable
    override fun SummaryContent(item: LibraryItem) {
        val anime = runBlocking { getAnime(item) }
        anime?.let {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = it.title?.english ?: it.title?.romaji ?: it.title?.native ?: item.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${it.format?.rawValue ?: "UNKNOWN"} • ${it.episodes ?: "?"} eps • ${it.status?.rawValue ?: "UNKNOWN"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                it.averageScore?.let { score ->
                    Text(
                        text = "Score: ${score / 10.0}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    @Composable
    override fun DescriptionContent(item: LibraryItem) {
        val anime = runBlocking { getAnime(item) }
        anime?.let {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Synopsis",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = it.description?.replace(Regex("<[^>]*>"), "") ?: "No synopsis available.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }

    @Composable
    override fun AttributeContent(item: LibraryItem) {
        val anime = runBlocking { getAnime(item) }
        anime?.let {
            Column(modifier = Modifier.padding(16.dp)) {
                AttributeRow("Format", it.format?.rawValue ?: "Unknown")
                AttributeRow("Source", it.source ?: "Unknown")
                AttributeRow("Season", "${it.season ?: "Unknown"} ${it.seasonYear ?: ""}")
                AttributeRow("Studios", it.studios.joinToString(",") ?: "Unknown")
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
    override fun SettingsScreen(onBack: () -> Unit) {
        AnimeSettingsScreen(metadata, genreCache, onBack)
    }

    private suspend fun getAnime(item: LibraryItem) = service
        .getAnimeById(item.id.toInt())
        .getOrNull()
}
