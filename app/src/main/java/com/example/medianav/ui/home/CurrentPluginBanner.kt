package com.example.medianav.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.medianav.R
import com.example.medianav.ui.navigation.PluginViewModel
import com.example.plugin_common.plugin.MediaPlugin

@Composable
internal fun CurrentPluginBanner(pluginViewModel: PluginViewModel) {
    val currentPlugin by pluginViewModel.currentPlugin.collectAsState()

    AnimatedContent(
        targetState = currentPlugin,
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp, 16.dp),
    ) { plugin ->
        plugin?.let { PluginBanner(it) }
            ?: MediaNavBanner()
    }
}

@Composable
private fun PluginBanner(plugin: MediaPlugin) {
    Image(
        painter = plugin.resources.banner,
        contentDescription = null,
        modifier = Modifier.height(160.dp)
    )
}

@Composable
private fun MediaNavBanner() {
    Image(
        painter = painterResource(R.drawable.ic_medianav_banner),
        contentDescription = null,
        modifier = Modifier.height(160.dp)
    )
}