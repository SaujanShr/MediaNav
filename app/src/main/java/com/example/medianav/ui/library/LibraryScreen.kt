package com.example.medianav.ui.library

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medianav.ui.library.list.LibraryHeader
import com.example.medianav.ui.library.list.LibraryMode
import com.example.medianav.ui.library.media.MediaDetailScreen
import com.example.medianav.ui.navigation.PluginViewModel
import com.example.plugin_common.library.LibraryItem

@Composable
fun LibraryScreen(
    libraryViewModel: LibraryViewModel = viewModel(),
    pluginViewModel: PluginViewModel
) {
    val currentPlugin by pluginViewModel.currentPlugin.collectAsState()
    val currentItem by libraryViewModel.currentItem.collectAsState()
    val mode by libraryViewModel.mode.collectAsState()

    LaunchedEffect(currentPlugin) {
        libraryViewModel.setPlugin(currentPlugin)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (currentItem != null) {
            MediaDetailScreen(
                viewModel = libraryViewModel,
                onBack = { libraryViewModel.clearItem() }
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                LibraryHeader(libraryViewModel)

                Box {
                    when (mode) {
                        is LibraryMode.Query -> QueryMode(
                            viewModel = libraryViewModel,
                            plugin = currentPlugin,
                            onItemClick = { item, itemsList ->
                                libraryViewModel.selectItem(item, itemsList)
                            }
                        )
                        is LibraryMode.List -> Column(modifier = Modifier.fillMaxSize()) {
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
    }
}
