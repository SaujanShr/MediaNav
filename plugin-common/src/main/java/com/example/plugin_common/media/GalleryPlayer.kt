package com.example.plugin_common.media

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState

@Composable
fun GalleryPlayer(
    thumbnail: Media,
    gallery: List<Media>,
    initialPage: Int = 0,
    onPageChange: ((Int) -> Unit)? = null
) {
    var isFullscreen by rememberSaveable { mutableStateOf(false) }
    var currentPage by rememberSaveable { mutableIntStateOf(initialPage) }

    Thumbnail(thumbnail, onClick = { isFullscreen = true })

    if (isFullscreen) {
        FullscreenGalleryDialog(
            gallery = gallery.ifEmpty { listOf(thumbnail) },
            initialPage = currentPage,
            onPageChanged = { page ->
                currentPage = page
                onPageChange?.invoke(page)
            },
            onDismiss = { isFullscreen = false }
        )
    }
}

@Composable
private fun Thumbnail(thumbnail: Media, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        SubcomposeAsyncImage(
            model = thumbnail.url,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun FullscreenGallery(
    gallery: List<Media>,
    initialPage: Int,
    onPageChanged: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (gallery.size == 1) {
            SingleZoomableImage(gallery[0])
        } else {
            ZoomableImagePager(
                gallery = gallery,
                initialPage = initialPage,
                onPageChanged = onPageChanged
            )
        }

        CloseButton(onClick = onDismiss)
    }
}

@Composable
private fun SingleZoomableImage(image: Media) {
    ZoomableAsyncImage(
        model = image.url,
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        state = rememberZoomableImageState()
    )
}

@Composable
private fun ZoomableImagePager(
    gallery: List<Media>,
    initialPage: Int,
    onPageChanged: (Int) -> Unit
) {
    val validInitialPage = initialPage.coerceIn(0, gallery.size - 1)
    val pagerState = rememberPagerState(
        initialPage = validInitialPage,
        pageCount = { gallery.size }
    )

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            onPageChanged(page)
        }
    }

    VerticalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        ZoomableAsyncImage(
            model = gallery[page].url,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            state = rememberZoomableImageState()
        )
    }
}

@Composable
private fun BoxScope.CloseButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(16.dp)
            .statusBarsPadding()
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = null,
            tint = Color.White
        )
    }
}

@Composable
private fun FullscreenGalleryDialog(
    gallery: List<Media>,
    initialPage: Int,
    onPageChanged: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        FullscreenGallery(
            gallery = gallery,
            initialPage = initialPage,
            onPageChanged = onPageChanged,
            onDismiss = onDismiss
        )
    }
}