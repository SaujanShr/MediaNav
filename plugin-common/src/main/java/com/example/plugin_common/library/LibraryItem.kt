package com.example.plugin_common.library

data class LibraryItem(
    val id: String,
    val title: String,
    val thumbnailUrl: String,
    val index: Int = 0,
    val saved: Boolean = false,
    val status: LibraryItemStatus = LibraryItemStatus.NONE,
    val lastAccessed: Long = System.currentTimeMillis()
)