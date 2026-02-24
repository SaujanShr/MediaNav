package com.example.medianav.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medianav.ui.navigation.PluginViewModel
import com.example.plugin_common.settings.SettingsContent
import com.example.plugin_common.settings.SettingsGroup
import com.example.plugin_common.shared.ErrorBannerList

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel = viewModel(),
    pluginViewModel: PluginViewModel
) {
    val settingsGroups = listOf(
        SettingsGroup(
            "Plugins",
            listOf(
                installPluginSetting(settingsViewModel),
                uninstallPluginSetting(settingsViewModel, pluginViewModel),
                togglePluginSetting(settingsViewModel, pluginViewModel)
            )
        ),
        SettingsGroup(
            "Display",
            listOf(
                themeSetting(settingsViewModel)
            )
        )
    )

    SettingsContent(settingsGroups = settingsGroups)
}