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
    fun Remote(url: String) {
        Image(url, aspectRatio)
    }

    @Composable
    fun Local(file: File) {
        Image(file, aspectRatio)
    }
}

@Composable
private fun Image(image: Any, aspectRatio: Float) {
    Box(modifier = Modifier.aspectRatio(aspectRatio)) {
        SubcomposeAsyncImage(
            model = image,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}
