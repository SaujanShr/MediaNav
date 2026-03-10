package com.example.plugin_anime.ui

import androidx.compose.runtime.Composable
import com.example.plugin_anime.domain.Anime
import com.example.plugin_common.plugin.ui.summary.Attribute
import com.example.plugin_common.plugin.ui.summary.Summary

@Composable
fun AnimeSummary(anime: Anime?) {
    anime?.let {
        val attributes = buildList {
            add(Attribute("Format", it.format?.rawValue ?: "UNKNOWN"))
            add(Attribute("Status", it.status?.rawValue ?: "UNKNOWN"))
            val seasonInfo = buildString {
                it.season?.let { season -> append(season) }
                it.seasonYear?.let { year ->
                    if (isNotEmpty()) append(" ")
                    append(year)
                }
            }
            if (seasonInfo.isNotEmpty()) {
                add(Attribute("Season", seasonInfo))
            }
            it.averageScore?.let { score ->
                add(Attribute("Score", "$score%"))
            }
        }

        Summary(
            title = it.title.romaji ?: "",
            attributes = attributes
        )
    }
}