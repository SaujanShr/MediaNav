package com.example.medianav.ui.library.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.medianav.ui.library.LibraryViewModel
import com.example.plugin_common.library.LibraryItemStatus

@Composable
internal fun LibraryHeader(libraryViewModel: LibraryViewModel) {
    val mode by libraryViewModel.mode.collectAsState()

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Title(
            libraryViewModel = libraryViewModel,
            mode = mode
        )
        Selector(
            libraryViewModel = libraryViewModel,
            mode = mode
        )
    }
}

@Composable
private fun Title(
    libraryViewModel: LibraryViewModel,
    mode: LibraryMode
) {
    val currentPlugin by libraryViewModel.currentPlugin.collectAsState()

    val title = when (mode) {
        is LibraryMode.Query -> currentPlugin?.metadata?.name ?: "Library"
        is LibraryMode.List -> when (mode.mode) {
            ListMode.SAVED -> "Saved"
            ListMode.LIKED -> "Liked"
            ListMode.VIEWED -> "Viewed"
        }
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(end = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        if (mode is LibraryMode.Query && mode.mode == QueryMode.NEW_ONLY) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
        else if (mode.isEdit) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun Selector(
    libraryViewModel: LibraryViewModel,
    mode: LibraryMode
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LibraryItemStatus.entries.forEach { status ->
            val icon = when (status) {
                LibraryItemStatus.LIKED -> Icons.Default.Favorite
                LibraryItemStatus.VIEWED -> Icons.Default.Visibility
                LibraryItemStatus.NONE -> Icons.AutoMirrored.Filled.ViewList
            }

            val isSelected = mode is LibraryMode.List &&
                mode.mode == when (status) {
                    LibraryItemStatus.LIKED -> ListMode.LIKED
                    LibraryItemStatus.VIEWED -> ListMode.VIEWED
                    LibraryItemStatus.NONE -> null
                } && mode.mode != ListMode.SAVED

            IconButton(
                onClick = {
                    if (isSelected && status != LibraryItemStatus.NONE) {
                        libraryViewModel.toggleEditMode()
                    } else {
                        libraryViewModel.setStatus(status)
                    }
                }
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = status.name,
                    tint = if (isSelected) {
                        if (mode is LibraryMode.Query && mode.mode == QueryMode.NEW_ONLY) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }

        when (mode) {
            is LibraryMode.Query -> {
                IconButton(onClick = { /* do nothing */ }) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            is LibraryMode.List -> {
                val isSavedMode = mode.mode == ListMode.SAVED
                IconButton(
                    onClick = {
                        if (isSavedMode) {
                            libraryViewModel.toggleSavedSortOrder()
                        } else {
                            libraryViewModel.setSavedMode()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = "Saved",
                        tint = if (isSavedMode) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}
