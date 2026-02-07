package com.example.plugin_anime

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.plugin_anime.domain.Anime
import com.example.plugin_common.media.DetailSection
import com.example.plugin_common.media.InfoRow

@Composable
internal fun AnimeDetails(anime: Anime?) {
    if (anime == null) {
        Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
            Text("Anime details not available")
        }
        return
    }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Synopsis
        if (!anime.synopsis.isNullOrBlank()) {
            DetailSection("Synopsis", anime.synopsis)
        }

        // Information
        DetailSection("Information") {
            InfoRow("Type", anime.type?.value ?: "Unknown")
            InfoRow("Episodes", anime.episodes?.toString() ?: "Unknown")
            InfoRow("Status", anime.status?.value ?: "Unknown")
            InfoRow("Aired", anime.aired.prop.string ?: "Unknown")
            InfoRow("Duration", anime.duration ?: "Unknown")
            InfoRow("Rating", anime.rating?.value ?: "Unknown")
            InfoRow("Score", anime.score?.toString() ?: "N/A")
            InfoRow("Rank", anime.rank?.let { "#$it" } ?: "N/A")
            InfoRow("Popularity", anime.popularity?.let { "#$it" } ?: "N/A")
        }

        // Genres
        if (anime.genres.isNotEmpty() || anime.explicitGenres.isNotEmpty()) {
            val allGenres = (anime.genres + anime.explicitGenres).joinToString { it.name }
            DetailSection("Genres", allGenres)
        }

        // Studios
        if (anime.studios.isNotEmpty()) {
            DetailSection("Studios", anime.studios.joinToString { it.name })
        }

        // Themes
        if (anime.themes.isNotEmpty()) {
            DetailSection("Themes", anime.themes.joinToString { it.name })
        }

        // Background
        if (!anime.background.isNullOrBlank()) {
            DetailSection("Background", anime.background)
        }
    }
}

@Composable
internal fun AnimeSummary(anime: Anime?) {
    Column(
        modifier = Modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        InfoRow("Status", anime?.status?.value ?: "Unknown")
        InfoRow("Score", anime?.score?.toString() ?: "N/A")
        InfoRow("Type", anime?.type?.value ?: "Unknown")
    }
}
