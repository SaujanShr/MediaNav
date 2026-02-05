package com.example.plugin_anime

import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.plugin_anime.domain.Anime
import com.example.plugin_common.media.GalleryPlayer
import com.example.plugin_common.media.Media
import com.example.plugin_common.media.MediaType

@Composable
internal fun AnimeThumbnail(anime: Anime?) {
    Surface(
        modifier = Modifier.size(160.dp),
        color = Color.Black
    ) {
        ThumbnailContent(anime)
    }
}

@Composable
private fun ThumbnailContent(anime: Anime?) {
    if (anime == null || anime.images.jpg.largeImageUrl == null) {
        return MissingThumbnail()
    }
    val imageUrl = anime.images.jpg.largeImageUrl
    val media = Media(imageUrl, MediaType.IMAGE)
    GalleryPlayer(
        thumbnail = media, listOf()
    )
}

@Composable
private fun MissingThumbnail() = Text(
    text = "?",
    style = MaterialTheme.typography.headlineLarge
)