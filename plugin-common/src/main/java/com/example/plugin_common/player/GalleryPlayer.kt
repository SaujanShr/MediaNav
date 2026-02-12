package com.example.plugin_common.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import com.example.plugin_common.player.util.CloseButton
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState
import java.io.File

class GalleryPlayer {
    @Composable
    fun Remote(thumbnailUrl: String, galleryUrls: List<String>) {
        Gallery(thumbnailUrl, galleryUrls)
    }

    @Composable
    fun Local(thumbnailFile: File, galleryFiles: List<File>, ) {
        Gallery(thumbnailFile, galleryFiles)
    }
}

@Composable
private fun Gallery(thumbnail: Any, gallery: List<Any>) {
    var isFullscreen by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { isFullscreen = true }
    ) {
        SubcomposeAsyncImage(
            model = thumbnail,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }

    if (isFullscreen) {
        FullscreenGalleryDialog(
            gallery = gallery.ifEmpty { listOf(thumbnail) },
            onDismiss = { isFullscreen = false }
        )
    }
}

@Composable
private fun FullscreenGalleryDialog(
    gallery: List<Any>,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim)
        ) {
            val pagerState = rememberPagerState(
                initialPage = 0,
                pageCount = { gallery.size }
            )

            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                ZoomableAsyncImage(
                    model = gallery[page],
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    state = rememberZoomableImageState()
                )
            }

            CloseButton(onClick = onDismiss)
        }
    }
}