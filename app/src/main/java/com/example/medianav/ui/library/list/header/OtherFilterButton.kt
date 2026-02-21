package com.example.medianav.ui.library.list.header

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.example.medianav.ui.library.LibraryViewModel
import com.example.medianav.ui.library.mode.LibraryMode
import com.example.medianav.ui.library.mode.ListModeSort
import com.example.medianav.ui.library.mode.ListModeStatus

@Composable
internal fun OtherFilterButton(
    libraryViewModel: LibraryViewModel,
    mode: LibraryMode
) {
    when (mode) {
        is LibraryMode.Query -> {
            QueryFilterButton()
        }
        is LibraryMode.List -> {
            SavedModeButton(
                libraryViewModel = libraryViewModel,
                mode = mode
            )
        }
    }
}

@Composable
private fun QueryFilterButton() {
    IconButton(onClick = { /* TODO: Implement filter functionality */ }) {
        Icon(
            imageVector = Icons.Default.FilterList,
            contentDescription = "Filter",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SavedModeButton(
    libraryViewModel: LibraryViewModel,
    mode: LibraryMode.List
) {
    val isSelected = mode.status == ListModeStatus.SAVED

    IconButton(
        onClick = { handleSavedButtonClick(libraryViewModel, mode, isSelected) }
    ) {
        Icon(
            imageVector = Icons.Default.Bookmark,
            contentDescription = "Saved",
            tint = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

private fun handleSavedButtonClick(
    libraryViewModel: LibraryViewModel,
    mode: LibraryMode.List,
    isSelected: Boolean
) {
    if (!isSelected) {
        switchToSavedMode(libraryViewModel)
    } else {
        cycleSavedModeOptions(libraryViewModel, mode)
    }
}

private fun switchToSavedMode(libraryViewModel: LibraryViewModel) {
    libraryViewModel.setMode(
        LibraryMode.List(
            status = ListModeStatus.SAVED,
            sort = ListModeSort.BY_INDEX,
            isEdit = false
        )
    )
}

private fun cycleSavedModeOptions(
    libraryViewModel: LibraryViewModel,
    mode: LibraryMode.List
) {
    val newMode = when {
        mode.isEdit ->
            LibraryMode.List(
                status = ListModeStatus.SAVED,
                sort = ListModeSort.BY_INDEX,
                isEdit = false
            )
        mode.sort == ListModeSort.BY_INDEX ->
            LibraryMode.List(
                status = ListModeStatus.SAVED,
                sort = ListModeSort.BY_ACCESS,
                isEdit = false
            )
        else ->
            LibraryMode.List(
                status = ListModeStatus.SAVED,
                sort = ListModeSort.BY_INDEX,
                isEdit = true
            )
    }
    libraryViewModel.setMode(newMode)
}

