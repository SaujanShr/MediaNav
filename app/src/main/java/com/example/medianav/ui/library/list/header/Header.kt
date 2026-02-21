package com.example.medianav.ui.library.list.header

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.medianav.ui.library.LibraryViewModel

@Composable
fun LibraryHeader(libraryViewModel: LibraryViewModel) {
    val mode by libraryViewModel.mode.collectAsState()

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        LibraryTitle(
            libraryViewModel = libraryViewModel,
            mode = mode
        )
        LibrarySelector(
            libraryViewModel = libraryViewModel,
            mode = mode
        )
    }
}

