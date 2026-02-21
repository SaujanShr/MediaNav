package com.example.medianav.ui.library.mode

import com.example.plugin_common.library.LibraryItemStatus

sealed class LibraryMode {
    data class Query(val type: QueryModeType) : LibraryMode()
    data class List(
        val status: ListModeStatus,
        val sort: ListModeSort = ListModeSort.BY_INDEX,
        val isEdit: Boolean = false
    ) : LibraryMode()

    val isQuery = this is Query
    val isList = this is List
}



