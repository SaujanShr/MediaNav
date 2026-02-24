package com.example.plugin_common.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SettingsContent(
    settingsContentViewModel: SettingsContentViewModel = viewModel(),
    settingsGroups: List<SettingsGroup>
) {
    val expandedSetting by settingsContentViewModel.expandedSettingIndex.collectAsState()

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        var cumulativeIndex = 0

        settingsGroups.forEach { group ->
            group.Content(cumulativeIndex, expandedSetting) {
                settingsContentViewModel.toggleExpanded(it)
            }
            cumulativeIndex += group.settings.size
        }
    }
}


