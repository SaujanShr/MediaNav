package com.example.medianav.ui.media

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.medianav.ui.navigation.ItemViewModel
import com.example.plugin_common.library.LibraryItemStatus

@Composable
fun MediaScreen(viewModel: ItemViewModel) {
    val selectedItem by viewModel.selectedItem.collectAsState()
    val plugin by viewModel.currentPlugin.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        selectedItem?.let { item ->
            plugin?.let { p ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(150.dp)
                                .aspectRatio(75f / 106f)
                        ) {
                            p.PreviewContent(item)
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            p.SummaryContent(item)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        IconButton(onClick = { viewModel.toggleStatus(LibraryItemStatus.VIEWED) }) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "Viewed",
                                tint = if (item.status == LibraryItemStatus.VIEWED) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                        IconButton(onClick = { viewModel.toggleStatus(LibraryItemStatus.SAVED) }) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Liked",
                                tint = if (item.status == LibraryItemStatus.SAVED) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }

                    p.DescriptionContent(item)
                    p.AttributeContent(item)
                }
            }
        }
    }
}
