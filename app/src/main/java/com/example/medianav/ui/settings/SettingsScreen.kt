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
import com.example.medianav.ui.shared.ErrorBanner

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel = viewModel(),
    pluginViewModel: PluginViewModel
) {

    val errorMessages = remember { mutableStateListOf<String>() }

    LaunchedEffect(settingsViewModel) {
        settingsViewModel.errors.collect { errorMessages.add(it) }
    }

    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingsSectionHeader("Plugins")

            InstallPluginSetting(settingsViewModel)
            UninstallPlugin(settingsViewModel, pluginViewModel)
            TogglePluginSetting(settingsViewModel, pluginViewModel)

            SettingsSectionHeader("Display")
            ThemeSetting(settingsViewModel)
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            errorMessages.forEach { message ->
                ErrorBanner(
                    message = message,
                    onDismiss = { errorMessages.remove(message) }
                )
            }
        }
    }
}


@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(8.dp)
    )
}