package com.example.plugin_common.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.plugin_common.shared.ErrorBannerList

@Composable
fun SettingsContent(
    settingsContentViewModel: SettingsContentViewModel = viewModel(),
    settingsGroups: List<SettingsGroup>
) {
    val errorMessages = remember { mutableStateListOf<String>() }
    val expandedSetting by settingsContentViewModel.expandedSettingIndex.collectAsState()

    LaunchedEffect(settingsContentViewModel) {
        settingsContentViewModel.errors.collect { errorMessages.add(it) }
    }

    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
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

        ErrorBannerList(errorMessages, Modifier.align(Alignment.TopCenter))
    }
}


