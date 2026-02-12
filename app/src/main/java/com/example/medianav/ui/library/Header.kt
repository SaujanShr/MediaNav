package com.example.medianav.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
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
    val isFiltered by libraryViewModel.isQueryFiltered.collectAsState()
    val currentPlugin by libraryViewModel.currentPlugin.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val title = when (selectedStatus) {
            LibraryItemStatus.NONE -> currentPlugin?.metadata?.name ?: "Library"
            LibraryItemStatus.SAVED -> "Saved"
            LibraryItemStatus.VIEWED -> "Viewed"
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            if (selectedStatus == LibraryItemStatus.NONE && isFiltered) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filtered",
                    modifier = Modifier.padding(start = 8.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        StatusSelector(
            selectedStatus = selectedStatus,
            isFiltered = isFiltered,
            onStatusSelected = { libraryViewModel.setStatus(it) }
        )
    }
}

@Composable
private fun StatusSelector(
    selectedStatus: LibraryItemStatus,
    isFiltered: Boolean,
    onStatusSelected: (LibraryItemStatus) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LibraryItemStatus.entries.forEach { status ->
            val icon = when (status) {
                LibraryItemStatus.SAVED -> Icons.Default.Favorite
                LibraryItemStatus.VIEWED -> Icons.Default.Visibility
                LibraryItemStatus.NONE -> Icons.AutoMirrored.Filled.ViewList
            }

            IconButton(
                onClick = { onStatusSelected(status) }
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selectedStatus == status) {
                        if (status == LibraryItemStatus.NONE && isFiltered) {
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
    }
}
