package com.example.medianav.ui.library.media

import android.content.pm.ActivityInfo
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.medianav.ui.animation.SwipeableContent
import com.example.medianav.ui.library.LibraryViewModel
import com.example.medianav.ui.shared.LockScreenOrientation
import com.example.plugin_common.library.LibraryItem
import com.example.plugin_common.library.LibraryItemStatus
import com.example.plugin_common.plugin.MediaPlugin

@Composable
fun MediaScreen(viewModel: LibraryViewModel) {
    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)

    val currentItem by viewModel.currentItem.collectAsState()
    val plugin by viewModel.currentPlugin.collectAsState()
    val canNavigateNext by viewModel.canNavigateNext.collectAsState()
    val canNavigatePrevious by viewModel.canNavigatePrevious.collectAsState()

    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
    ) {
        MediaTopBar(currentItem?.title)

        SwipeableContent(
            contentId = currentItem?.id,
            canNavigateNext = canNavigateNext,
            canNavigatePrevious = canNavigatePrevious,
            onNavigateNext = { viewModel.navigateNext() },
            onNavigatePrevious = { viewModel.navigatePrevious() }
        ) { _, _ ->
            MediaDetailContent(
                item = currentItem,
                plugin = plugin,
                viewModel = viewModel
            )
        }
    }
}

@Composable
private fun MediaTopBar(title: String?) {
    Text(
        text = title ?: "",
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)
    )
}

@Composable
private fun MediaDetailContent(
    item: LibraryItem?,
    plugin: MediaPlugin?,
    viewModel: LibraryViewModel
) {
    item?.let { currentItem ->
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
                        p.PreviewContent(currentItem)
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        p.SummaryContent(currentItem)
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
                            contentDescription = null,
                            tint = if (currentItem.status == LibraryItemStatus.VIEWED) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                    IconButton(onClick = { viewModel.toggleStatus(LibraryItemStatus.LIKED) }) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = if (currentItem.status == LibraryItemStatus.LIKED) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                    IconButton(onClick = { viewModel.toggleSaved() }) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = if (currentItem.saved) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }

                p.DescriptionContent(currentItem)
                p.AttributeContent(currentItem)
            }
        }
    }
}

