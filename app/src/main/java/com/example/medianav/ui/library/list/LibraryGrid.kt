package com.example.medianav.ui.library.list

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import com.example.medianav.ui.library.LibraryViewModel
import com.example.medianav.ui.library.mode.LibraryMode
import com.example.plugin_common.library.LibraryItem
import com.example.plugin_common.plugin.MediaPlugin

@SuppressLint("MutableCollectionMutableState")
@Composable
internal fun LibraryGrid(
    viewModel: LibraryViewModel,
    plugin: MediaPlugin?,
    mode: LibraryMode,
    onItemClick: (LibraryItem, List<LibraryItem>) -> Unit
) {
    val pagerState = when (mode) {
        is LibraryMode.Query -> viewModel.queryPager.collectAsState()
        is LibraryMode.List -> viewModel.listPager.collectAsState()
    }
    val pager = pagerState.value

    if (pager != null) {
        key(pager, mode::class) {
            PagingGrid(
                pager = pager,
                plugin = plugin,
                onItemClick = onItemClick
            )
        }
    }
}

