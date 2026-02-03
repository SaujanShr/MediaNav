package com.example.medianav.ui.shared

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.medianav.util.toTitleCase
import com.example.plugin_common.plugin.MediaPlugin

@Composable
fun Plugin(
    plugin: MediaPlugin,
    onClick: ((MediaPlugin) -> Unit)? = null,
    content: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .let {
                if (onClick != null) {
                    it.clickable { onClick(plugin) }
                } else it
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        PluginContent(plugin)
        content?.invoke()
    }
}

@Composable
private fun PluginContent(plugin: MediaPlugin) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        PluginIcon(plugin.info.icon)

        Spacer(Modifier.width(20.dp))

        Column {
            Text(
                text = plugin.info.name,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = plugin.info.category.value.toTitleCase(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PluginIcon(icon: Painter) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.24f))
    ) {
        Image(
            painter = icon,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            contentScale = ContentScale.Fit
        )
    }
}