package com.example.medianav.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.medianav.ui.navigation.PluginViewModel

@Composable
fun HomeScreen(pluginViewModel: PluginViewModel) {
    val pluginSettings by pluginViewModel.pluginSettings.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            CurrentPluginBanner(pluginViewModel)
            CurrentPluginHeader(pluginViewModel)
            PluginTab(pluginViewModel)
        }

        if (pluginSettings != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                pluginSettings?.SettingsScreen(onBack = { pluginViewModel.closePluginSettings() })
            }
        }
    }
}
