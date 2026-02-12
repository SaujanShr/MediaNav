package com.example.plugin_common.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
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

class PreviewPlayer(val aspectRatio: Float) {
    @Composable
    fun Remote(thumbnailUrl: String, previewUrls: List<String>) {
        Preview(thumbnailUrl, previewUrls, aspectRatio)
    }

    @Composable
    fun Local(thumbnailFile: File, previewFiles: List<File>) {
        Preview(thumbnailFile, previewFiles, aspectRatio)
    }
}

@Composable
private fun Preview(thumbnail: Any, previews: List<Any>, aspectRatio: Float) {
    var isFullscreen by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .aspectRatio(aspectRatio)
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
        FullscreenPreviewDialog(
            gallery = previews.ifEmpty { listOf(thumbnail) },
            onDismiss = { isFullscreen = false }
        )
    }
}

@Composable
private fun FullscreenPreviewDialog(
    gallery: List<Any>,
    onDismiss: () -> Unit
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
            val isLooping = gallery.size > 1
            val pagerState = rememberPagerState(
                initialPage =
                    if (isLooping) (Int.MAX_VALUE / 2) - ((Int.MAX_VALUE / 2) % gallery.size)
                    else 0,
                pageCount = {
                    if (isLooping) Int.MAX_VALUE else gallery.size
                }
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                ZoomableAsyncImage(
                    model = gallery[page % gallery.size],
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    state = rememberZoomableImageState()
                )
            }

            CloseButton(onClick = onDismiss)
        }
    }
}