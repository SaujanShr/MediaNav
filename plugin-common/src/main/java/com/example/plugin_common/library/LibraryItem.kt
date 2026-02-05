package com.example.plugin_common.library

import com.example.plugin_common.media.Media

data class LibraryItem(
    val id: String,
    val title: String,
    val status: LibraryItemStatus = LibraryItemStatus.NONE
)