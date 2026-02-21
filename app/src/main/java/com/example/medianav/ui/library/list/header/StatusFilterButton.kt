package com.example.medianav.ui.library.list.header

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.example.medianav.ui.library.LibraryViewModel
import com.example.medianav.ui.library.mode.LibraryMode
import com.example.medianav.ui.library.mode.ListModeSort
import com.example.medianav.ui.library.mode.ListModeStatus
import com.example.medianav.ui.library.mode.QueryModeType
import com.example.plugin_common.library.LibraryItemStatus

@Composable
internal fun StatusFilterButton(
    libraryViewModel: LibraryViewModel,
    status: LibraryItemStatus,
    mode: LibraryMode,
) {
    val icon = getIconForStatus(status)
    val isSelected = isStatusSelected(status, mode)

    IconButton(
        onClick = {
            handleStatusButtonClick(libraryViewModel, status, mode, isSelected)
        }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = status.name,
            tint = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

private fun getIconForStatus(status: LibraryItemStatus) = when (status) {
    LibraryItemStatus.NONE -> Icons.AutoMirrored.Filled.ViewList
    LibraryItemStatus.VIEWED -> Icons.Default.Visibility
    LibraryItemStatus.LIKED -> Icons.Default.Favorite
}

private fun isStatusSelected(status: LibraryItemStatus, mode: LibraryMode): Boolean {
    return when (mode) {
        is LibraryMode.Query -> status == LibraryItemStatus.NONE
        is LibraryMode.List -> when (mode.status) {
            ListModeStatus.VIEWED -> status == LibraryItemStatus.VIEWED
            ListModeStatus.LIKED -> status == LibraryItemStatus.LIKED
            else -> false
        }
    }
}

private fun handleStatusButtonClick(
    libraryViewModel: LibraryViewModel,
    status: LibraryItemStatus,
    mode: LibraryMode,
    isSelected: Boolean
) {
    if (!isSelected) {
        switchToDefaultMode(libraryViewModel, status)
    } else {
        cycleCurrentModeOptions(libraryViewModel, mode)
    }
}

private fun switchToDefaultMode(
    libraryViewModel: LibraryViewModel,
    status: LibraryItemStatus
) {
    when (status) {
        LibraryItemStatus.NONE ->
            libraryViewModel.setMode(
                LibraryMode.Query(type = QueryModeType.ALL)
            )
        LibraryItemStatus.VIEWED ->
            libraryViewModel.setMode(
                LibraryMode.List(
                    status = ListModeStatus.VIEWED,
                    sort = ListModeSort.BY_INDEX,
                    isEdit = false
                )
            )
        LibraryItemStatus.LIKED ->
            libraryViewModel.setMode(
                LibraryMode.List(
                    status = ListModeStatus.LIKED,
                    sort = ListModeSort.BY_INDEX,
                    isEdit = false
                )
            )
    }
}

private fun cycleCurrentModeOptions(
    libraryViewModel: LibraryViewModel,
    mode: LibraryMode
) {
    when (mode) {
        is LibraryMode.Query -> cycleQueryModeOptions(libraryViewModel, mode)
        is LibraryMode.List -> cycleListModeOptions(libraryViewModel, mode)
    }
}

private fun cycleQueryModeOptions(
    libraryViewModel: LibraryViewModel,
    mode: LibraryMode.Query
) {
    val newType = if (mode.type == QueryModeType.ALL) {
        QueryModeType.NEW_ONLY
    } else {
        QueryModeType.ALL
    }
    libraryViewModel.setMode(LibraryMode.Query(newType))
}

private fun cycleListModeOptions(
    libraryViewModel: LibraryViewModel,
    mode: LibraryMode.List
) {
    val newMode = when {
        mode.isEdit ->
            LibraryMode.List(
                status = mode.status,
                sort = ListModeSort.BY_INDEX,
                isEdit = false
            )
        mode.sort == ListModeSort.BY_INDEX ->
            LibraryMode.List(
                status = mode.status,
                sort = ListModeSort.BY_ACCESS,
                isEdit = false
            )
        else ->
            LibraryMode.List(
                status = mode.status,
                sort = ListModeSort.BY_INDEX,
                isEdit = true
            )
    }
    libraryViewModel.setMode(newMode)
}

