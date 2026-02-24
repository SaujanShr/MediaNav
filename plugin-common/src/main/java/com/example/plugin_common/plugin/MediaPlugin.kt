package com.example.plugin_common.plugin

import androidx.compose.runtime.Composable
import com.example.custom_paging.paging.Pager
import com.example.plugin_common.library.LibraryItem
import com.example.plugin_common.library.LibraryQuery
import com.example.plugin_common.library.schema.QuerySchema

interface MediaPlugin {
    val metadata: PluginMetadata
    val resources: PluginResources
    val querySchema: QuerySchema

    suspend fun initialize()

    fun getPager(query: LibraryQuery?): Pager<LibraryItem>

    @Composable
    fun Thumbnail(item: LibraryItem)

    @Composable
    fun PreviewContent(item: LibraryItem)

    @Composable
    fun SummaryContent(item: LibraryItem)

    @Composable
    fun DescriptionContent(item: LibraryItem)

    @Composable
    fun AttributeContent(item: LibraryItem)

    @Composable
    fun PlayableContent(item: LibraryItem)

    @Composable
    fun SettingsScreen(onBack: () -> Unit)
}
