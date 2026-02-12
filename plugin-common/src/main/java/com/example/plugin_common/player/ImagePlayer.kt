package com.example.plugin_common.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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

class ImagePlayer {
    @Composable
    fun Remote(url: String) {
        Image(url)
    }

    @Composable
    fun Local(file: File) {
        Image(file)
    }
}

@Composable
private fun Image(image: Any) {
    var isFullscreen by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { isFullscreen = true }
    ) {
        SubcomposeAsyncImage(
            model = image,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }

    if (isFullscreen) {
        FullscreenImageDialog(
            model = image,
            onDismiss = { isFullscreen = false }
        )
    }
}

@Composable
private fun FullscreenImageDialog(
    model: Any,
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
            ZoomableAsyncImage(
                model = model,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                state = rememberZoomableImageState()
            )

            CloseButton(onClick = onDismiss)
        }
    }
}
