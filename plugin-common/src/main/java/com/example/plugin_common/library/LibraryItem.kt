package com.example.plugin_common.library

data class LibraryItem(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val thumbnailUrl: String,
    val status: LibraryItemStatus = LibraryItemStatus.NONE
)