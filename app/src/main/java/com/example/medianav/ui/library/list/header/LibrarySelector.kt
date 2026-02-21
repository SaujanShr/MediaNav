package com.example.medianav.ui.library.list.header

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.example.medianav.ui.library.LibraryViewModel
import com.example.medianav.ui.library.mode.LibraryMode
import com.example.plugin_common.library.LibraryItemStatus

@Composable
internal fun LibrarySelector(
    libraryViewModel: LibraryViewModel,
    mode: LibraryMode
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LibraryItemStatus.entries.forEach { status ->
            StatusFilterButton(
                libraryViewModel = libraryViewModel,
                status = status,
                mode = mode
            )
        }
        OtherFilterButton(
            libraryViewModel = libraryViewModel,
            mode = mode
        )
    }
}

