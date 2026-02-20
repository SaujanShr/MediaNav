package com.example.medianav.ui.library

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
import com.example.plugin_common.library.LibraryItemStatus

@Composable
internal fun LibraryHeader(libraryViewModel: LibraryViewModel) {
    val selectedStatus by libraryViewModel.selectedStatus.collectAsState()
    val currentPlugin by libraryViewModel.currentPlugin.collectAsState()
    val mode by libraryViewModel.mode.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val title = when {
            mode == LibraryMode.SAVED || mode == LibraryMode.SAVED_BY_DATE -> "Saved"
            selectedStatus == LibraryItemStatus.LIKED -> "Liked"
            selectedStatus == LibraryItemStatus.VIEWED -> "Viewed"
            else -> currentPlugin?.metadata?.name ?: "Library"
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (mode == LibraryMode.EDIT) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Mode",
                    modifier = Modifier.padding(end = 8.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            if (mode == LibraryMode.QUERY_NEW_ONLY) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filtered",
                    modifier = Modifier.padding(start = 8.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        StatusSelector(
            libraryViewModel = libraryViewModel,
            selectedStatus = selectedStatus,
            mode = mode,
            onStatusSelected = { libraryViewModel.setStatus(it) },
            onSavedClick = { libraryViewModel.setSavedMode() },
            onSavedDoubleClick = { libraryViewModel.toggleSavedSortOrder() }
        )
    }
}

@Composable
private fun StatusSelector(
    libraryViewModel: LibraryViewModel,
    selectedStatus: LibraryItemStatus,
    mode: LibraryMode,
    onStatusSelected: (LibraryItemStatus) -> Unit,
    onSavedClick: () -> Unit,
    onSavedDoubleClick: () -> Unit,
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

            val isSelected = selectedStatus == status && mode != LibraryMode.SAVED && mode != LibraryMode.SAVED_BY_DATE

            IconButton(
                onClick = {
                    if (isSelected && status != LibraryItemStatus.NONE) {
                        libraryViewModel.toggleEditMode()
                    } else {
                        onStatusSelected(status)
                    }
                }
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = status.name,
                    tint = if (isSelected) {
                        if (mode == LibraryMode.QUERY_NEW_ONLY) {
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
            LibraryMode.QUERY, LibraryMode.QUERY_NEW_ONLY -> {
                IconButton(onClick = { /* do nothing */ }) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            LibraryMode.LIST, LibraryMode.EDIT, LibraryMode.SAVED, LibraryMode.SAVED_BY_DATE -> {
                val isSavedMode = mode == LibraryMode.SAVED || mode == LibraryMode.SAVED_BY_DATE
                IconButton(
                    onClick = {
                        if (isSavedMode) {
                            onSavedDoubleClick()
                        } else {
                            onSavedClick()
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