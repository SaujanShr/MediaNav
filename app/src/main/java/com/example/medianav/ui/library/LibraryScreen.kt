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
import com.example.medianav.ui.library.list.LibraryGrid
import com.example.medianav.ui.library.list.header.LibraryHeader
import com.example.medianav.ui.library.media.MediaScreen
import com.example.medianav.ui.navigation.PluginViewModel

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
        Column(modifier = Modifier.fillMaxSize()) {
            LibraryHeader(libraryViewModel)
            LibraryGrid(
                viewModel = libraryViewModel,
                plugin = currentPlugin,
                mode = mode,
                onItemClick = { item, itemsList ->
                    libraryViewModel.selectItem(item, itemsList)
                }
            )
        }
        if (currentItem != null) {
            MediaScreen(
                viewModel = libraryViewModel,
                onBack = { libraryViewModel.clearItem() }
            )
        }
    }
}
