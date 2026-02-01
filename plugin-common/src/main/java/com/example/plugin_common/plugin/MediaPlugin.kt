package com.example.plugin_common.plugin

import androidx.compose.runtime.Composable
import com.example.plugin_common.library.LibraryItem
import com.example.plugin_common.library.LibraryQuery
import com.example.plugin_common.library.schema.QuerySchema
import com.example.plugin_common.plugin.info.PluginInfo

interface MediaPlugin {
    val info: PluginInfo
    val querySchema: QuerySchema

    suspend fun getLibraryItems(
        offset: Int,
        limit: Int,
        query: LibraryQuery
    ) : List<LibraryItem>

    suspend fun getLibraryItemCount(query: LibraryQuery): Int

    @Composable
    fun DetailsContent(item: LibraryItem)

    @Composable
    fun DropdownContent(item: LibraryItem)
}