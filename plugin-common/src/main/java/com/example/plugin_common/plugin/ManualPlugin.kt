package com.example.plugin_common.plugin

import com.example.plugin_common.library.LibraryQuery
import kotlinx.coroutines.flow.StateFlow

interface ManualPlugin: MediaPlugin {
    val isSyncing: StateFlow<Boolean>
    suspend fun sync(query: LibraryQuery)
}