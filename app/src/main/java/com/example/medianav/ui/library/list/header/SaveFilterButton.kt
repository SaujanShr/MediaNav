@file:JvmName("SaveFilterButtonKt")

package com.example.medianav.ui.library.list.header

import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.medianav.ui.library.LibraryViewModel
import com.example.medianav.ui.library.mode.LibraryMode
import com.example.medianav.ui.library.mode.ListModeSort
import com.example.medianav.ui.library.mode.ListModeStatus

@Composable
internal fun SaveFilterButton(
    libraryViewModel: LibraryViewModel,
    mode: LibraryMode
) {
    val isSelected = mode is LibraryMode.List && mode.status == ListModeStatus.SAVED

    Icon(
        imageVector = Icons.Default.Bookmark,
        contentDescription = null,
        tint =
            if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.combinedClickable(
            onClick = { handleSavedButtonClick(libraryViewModel, mode, isSelected) },
            onLongClick = { handleSavedButtonLongPress(libraryViewModel, isSelected) }
        )
    )
}

private fun handleSavedButtonClick(
    libraryViewModel: LibraryViewModel,
    mode: LibraryMode,
    isSelected: Boolean
) {
    if (!isSelected) {
        switchToSavedMode(libraryViewModel)
    } else {
        cycleSavedModeOptions(libraryViewModel, mode as LibraryMode.List)
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
                isEdit = false
            )
    }
    libraryViewModel.setMode(newMode)
}

private fun handleSavedButtonLongPress(
    libraryViewModel: LibraryViewModel,
    isSelected: Boolean
) {
    if (!isSelected) return

    libraryViewModel.setMode(
        LibraryMode.List(
            status = ListModeStatus.SAVED,
            sort = ListModeSort.BY_INDEX,
            isEdit = true
        )
    )
}