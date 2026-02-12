package com.example.medianav.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.plugin_common.library.LibraryItem
import com.example.plugin_common.plugin.MediaPlugin

@Composable
internal fun ListMode(
    viewModel: LibraryViewModel,
    plugin: MediaPlugin?,
    onItemClick: (LibraryItem) -> Unit
) {
    val items = viewModel.listItems.collectAsLazyPagingItems()

    if (items.itemCount == 0) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No items in this list")
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(items.itemCount) { index ->
                val item = items[index]
                if (item != null && plugin != null) {
                    LibraryItemCell(
                        item = item,
                        plugin = plugin,
                        onClick = { onItemClick(item) }
                    )
                }
            }
        }
    }
}
