package com.example.medianav.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.plugin_common.library.LibraryItemStatus

@Composable
internal fun LibraryHeader(libraryViewModel: LibraryViewModel) {
    val selectedStatus by libraryViewModel.selectedStatus.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatusSelector(
            selectedStatus = selectedStatus,
            onStatusSelected = { libraryViewModel.setStatus(it) }
        )
    }
}

@Composable
private fun StatusSelector(
    selectedStatus: LibraryItemStatus,
    onStatusSelected: (LibraryItemStatus) -> Unit
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

            IconButton(
                onClick = { onStatusSelected(status) }
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selectedStatus == status) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}