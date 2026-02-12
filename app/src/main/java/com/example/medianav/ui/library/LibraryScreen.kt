package com.example.medianav.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.example.medianav.ui.navigation.PluginViewModel
import com.example.medianav.ui.shared.ErrorBanner
import com.example.plugin_common.library.LibraryItem
import com.example.plugin_common.plugin.MediaPlugin

@Composable
fun LibraryScreen(
    libraryViewModel: LibraryViewModel = viewModel(),
    pluginViewModel: PluginViewModel,
    onItemClick: (LibraryItem, MediaPlugin) -> Unit
) {
    val errorMessages = remember { mutableStateListOf<String>() }

    LaunchedEffect(libraryViewModel) {
        libraryViewModel.errors.collect { errorMessages.add(it) }
    }

    val currentPlugin by pluginViewModel.currentPlugin.collectAsState()
    val mode by libraryViewModel.mode.collectAsState(initial = LibraryMode.QUERY)

    LaunchedEffect(currentPlugin) {
        libraryViewModel.setPlugin(currentPlugin)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            LibraryHeader(libraryViewModel)

            Box(modifier = Modifier.weight(1f)) {
                when (mode) {
                    LibraryMode.QUERY -> QueryMode(
                        viewModel = libraryViewModel,
                        plugin = currentPlugin,
                        onItemClick = { item ->
                            currentPlugin?.let { onItemClick(item, it) }
                        }
                    )
                    LibraryMode.LIST -> Column(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.weight(1f)) {
                            ListMode(
                                viewModel = libraryViewModel,
                                plugin = currentPlugin,
                                onItemClick = { item ->
                                    currentPlugin?.let { onItemClick(item, it) }
                                }
                            )
                        }
                        PageBar(libraryViewModel)
                    }
                }
            }
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
