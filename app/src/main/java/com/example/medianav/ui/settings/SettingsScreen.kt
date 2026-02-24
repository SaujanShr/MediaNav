package com.example.medianav.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    val errorMessages = remember { mutableStateListOf<String>() }

    LaunchedEffect(settingsViewModel) {
        settingsViewModel.errors.collect { errorMessages.add(it) }
    }

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
    Box(modifier = Modifier.fillMaxSize()) {
        SettingsContent(settingsGroups = settingsGroups)
        ErrorBannerList(errorMessages, Modifier.align(Alignment.TopCenter))
    }
}