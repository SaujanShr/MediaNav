package com.example.medianav.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
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
    pluginViewModel: PluginViewModel
) {
    val errorMessages = remember { mutableStateListOf<String>() }

    LaunchedEffect(libraryViewModel) {
        libraryViewModel.errors.collect { errorMessages.add(it) }
    }

    val currentPlugin by pluginViewModel.currentPlugin.collectAsState()
    val currentPage by libraryViewModel.currentPage.collectAsState()
    val totalPages by libraryViewModel.totalPages.collectAsState()

    LaunchedEffect(currentPlugin) {
        libraryViewModel.setPlugin(currentPlugin)
        libraryViewModel.loadLibraryPage()
        libraryViewModel.loadLibraryPageNumbers()
    }

    LaunchedEffect(currentPage) {
        libraryViewModel.loadLibraryPage()
    }

    val libraryItems by libraryViewModel.items.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            LibraryHeader(libraryViewModel)

            Box(modifier = Modifier.weight(1f)) {
                LibraryItemList(
                    items = libraryItems,
                    plugin = currentPlugin,
                    libraryViewModel = libraryViewModel
                )
            }

            if (currentPlugin != null) {
                PageBar(
                    currentPage = currentPage,
                    totalPages = totalPages,
                    onPageSelected = { page -> libraryViewModel.setPage(page) }
                )
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

@Composable
private fun LibraryItemList(
    items: List<LibraryItem>,
    plugin: MediaPlugin?,
    libraryViewModel: LibraryViewModel,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No library items found")
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = modifier.fillMaxSize()
        ) {
            items(items) { item ->
                plugin?.let {
                    LibraryItemCard(
                        viewModel = libraryViewModel,
                        item = item,
                        plugin = it
                    )
                }
            }
        }
    }
}
