package com.example.plugin_common.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.SubcomposeAsyncImage
import java.io.File

data class ThumbnailPlayer(val aspectRatio: Float) {
    @Composable
    fun Remote(url: String, onClick: () -> Unit) {
        Image(url, aspectRatio, onClick)
    }

    @Composable
    fun Local(file: File, onClick: () -> Unit) {
        Image(file, aspectRatio, onClick)
    }
}

@Composable
private fun Image(image: Any, aspectRatio: Float, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(aspectRatio)
            .clickable { onClick() }
    ) {
        SubcomposeAsyncImage(
            model = image,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}
