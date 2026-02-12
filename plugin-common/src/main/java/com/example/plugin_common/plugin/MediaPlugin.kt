package com.example.plugin_common.plugin

import androidx.compose.runtime.Composable
import androidx.paging.PagingData
import com.example.plugin_common.library.LibraryItem
import com.example.plugin_common.library.LibraryQuery
import com.example.plugin_common.library.schema.QuerySchema
import kotlinx.coroutines.flow.Flow

interface MediaPlugin {
    val metadata: PluginMetadata
    val resources: PluginResources
    val querySchema: QuerySchema

    suspend fun initialize()

    fun getLibraryItems(query: LibraryQuery): Flow<PagingData<LibraryItem>>

    @Composable
    fun Thumbnail(item: LibraryItem, onClick: () -> Unit)

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
    fun SettingsContent()
}