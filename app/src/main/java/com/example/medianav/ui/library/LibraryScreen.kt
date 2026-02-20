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

@Composable
fun LibraryScreen(
    libraryViewModel: LibraryViewModel = viewModel(),
    pluginViewModel: PluginViewModel
) {
    val errorMessages = remember { mutableStateListOf<String>() }

    LaunchedEffect(libraryViewModel) {
        libraryViewModel.errors.collect { errorMessages.add(it) }
    }

    val currentPlugin by pluginViewModel.currentPlugin.collectAsState()
    val mode by libraryViewModel.mode.collectAsState()
    val selectedItem by libraryViewModel.selectedItem.collectAsState()

    LaunchedEffect(currentPlugin) {
        libraryViewModel.setPlugin(currentPlugin)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (selectedItem != null) {
            // Show media detail screen
            MediaDetailScreen(
                viewModel = libraryViewModel,
                onBack = { libraryViewModel.clearSelectedItem() }
            )
        } else {
            // Show library list
            Column(modifier = Modifier.fillMaxSize()) {
                LibraryHeader(libraryViewModel)

                Box {
                    when (mode) {
                        LibraryMode.QUERY, LibraryMode.QUERY_NEW_ONLY -> QueryMode(
                            viewModel = libraryViewModel,
                            plugin = currentPlugin,
                            onItemClick = { item ->
                                // For query mode, we'll pass an empty list since paging makes it complex
                                libraryViewModel.selectItem(item, emptyList())
                            }
                        )
                        LibraryMode.LIST, LibraryMode.EDIT, LibraryMode.SAVED, LibraryMode.SAVED_BY_DATE -> Column(modifier = Modifier.fillMaxSize()) {
                            ListMode(
                                viewModel = libraryViewModel,
                                plugin = currentPlugin,
                                onItemClick = { item: LibraryItem, itemsList: List<LibraryItem> ->
                                    libraryViewModel.selectItem(item, itemsList)
                                }
                            )
                            PageBar(libraryViewModel)
                        }
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
