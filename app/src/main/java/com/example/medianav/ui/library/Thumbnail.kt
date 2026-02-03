package com.example.medianav.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import com.example.plugin_common.library.LibraryItem

@Composable
internal fun Thumbnail(item: LibraryItem) {
    var showFullScreen by remember { mutableStateOf(false) }

    ThumbnailSmall(
        url = item.thumbnailUrl,
        onClick = { showFullScreen = true }
    )

    if (showFullScreen) {
        FullScreenThumbnailDialog(
            url = item.thumbnailUrl,
            onDismiss = { showFullScreen = false }
        )
    }
}

@Composable
private fun ThumbnailSmall(
    url: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(160.dp)
            .background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.small)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        SubcomposeAsyncImage(
            model = url,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            loading = {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.padding(16.dp)
                )
            }
        )
    }
}

@Composable
private fun FullScreenThumbnailDialog(
    url: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.1f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            SubcomposeAsyncImage(
                model = url,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
                loading = {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            )
        }
    }
}
