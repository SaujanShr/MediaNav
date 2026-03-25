package com.example.plugin_common.settings

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class SettingsManager(private val settingsGroups: List<SettingsGroup>) {
    @Composable
    fun Content() {
        val expandedSetting = remember { mutableStateOf<Setting?>(null) }
        val scrollState = rememberScrollState()

        SettingsContent(expandedSetting, scrollState)
    }

    @Composable
    private fun SettingsContent(expandedSetting: MutableState<Setting?>, scrollState: ScrollState) {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            settingsGroups.forEach { group ->
                group.Content(
                    expandedSetting = expandedSetting.value,
                    toggleExpanded = { setting ->
                        expandedSetting.value =
                            if (expandedSetting.value == setting) null
                            else setting
                    },
                    collapseExpanded = {
                        expandedSetting.value = null
                    }
                )
            }
        }
    }
}

