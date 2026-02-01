package com.example.plugin_common.plugin

import androidx.compose.runtime.Composable
import com.example.plugin_common.library.LibraryItem
import com.example.plugin_common.plugin.info.PluginInfo

interface MediaPlugin {
    val info: PluginInfo

    suspend fun getLibraryItems(
        offset: Int,
        limit: Int
    ) : List<LibraryItem>

    suspend fun getLibraryItemCount(): Int

    @Composable
    fun DetailsContent(item: LibraryItem)

    @Composable
    fun DropdownContent(item: LibraryItem)
}