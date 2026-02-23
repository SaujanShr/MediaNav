package com.example.medianav.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.medianav.ui.navigation.PluginViewModel

@Composable
fun HomeScreen(pluginViewModel: PluginViewModel) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        CurrentPluginBanner(pluginViewModel)
        CurrentPluginHeader(pluginViewModel)
        PluginTab(pluginViewModel)
    }
}
