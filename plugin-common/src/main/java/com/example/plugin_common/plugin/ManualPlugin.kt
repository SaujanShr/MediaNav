package com.example.plugin_common.plugin

import kotlinx.coroutines.flow.StateFlow

interface ManualPlugin {
    val isSyncing: StateFlow<Boolean>
    suspend fun sync()
}