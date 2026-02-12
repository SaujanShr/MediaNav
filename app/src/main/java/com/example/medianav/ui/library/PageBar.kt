package com.example.medianav.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FirstPage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
internal fun PageBar(libraryViewModel: LibraryViewModel) {
    val currentPage by libraryViewModel.currentPage.collectAsState()
    val totalPages by libraryViewModel.totalPages.collectAsState()

    var showJumpDialog by remember { mutableStateOf(false) }
    val onPageSelected = { page: Int ->
        libraryViewModel.setPage(page)
    }


    if (totalPages > 1) {
        Surface(
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row {
                    PageNavigationButton(
                        icon = Icons.Default.FirstPage,
                        enabled = currentPage > 0,
                        onClick = { onPageSelected(0) }
                    )
                    PageNavigationButton(
                        icon = Icons.AutoMirrored.Filled.NavigateBefore,
                        enabled = currentPage > 0,
                        onClick = { onPageSelected(currentPage - 1) }
                    )
                }

                PageNumbersList(
                    totalPages = totalPages,
                    currentPage = currentPage,
                    onPageSelected = onPageSelected,
                    modifier = Modifier.weight(1f)
                )
                PageNavigationButton(
                    icon = Icons.AutoMirrored.Filled.NavigateNext,
                    enabled = currentPage < totalPages - 1,
                    onClick = { onPageSelected(currentPage + 1) }
                )
                PageNavigationButton(
                    icon = Icons.Default.Edit,
                    enabled = true,
                    onClick = { showJumpDialog = true }
                )
            }
        }
    }

    if (showJumpDialog) {
        JumpToPageDialog(
            totalPages = totalPages,
            onPageSelected = onPageSelected,
            onDismiss = { showJumpDialog = false }
        )
    }
}

@Composable
private fun JumpToPageDialog(
    totalPages: Int,
    onPageSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf("") }
    val pageNumber = text.toIntOrNull()
    val isValid = pageNumber != null && pageNumber in 1..totalPages

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Jump to Page") },
        text = {
            Column {
                Text("Enter page number (1-$totalPages)")
                OutlinedTextField(
                    value = text,
                    onValueChange = { input -> 
                        text = input.filter { it.isDigit() } 
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    singleLine = true,
                    placeholder = { Text("Page...") }
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = isValid,
                onClick = {
                    pageNumber?.let { onPageSelected(it - 1) }
                    onDismiss()
                }
            ) {
                Text("Go")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun PageNavigationButton(
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        enabled = enabled,
        onClick = onClick,
        modifier = Modifier.size(32.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun PageNumbersList(
    totalPages: Int,
    currentPage: Int,
    onPageSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val lazyListState = rememberLazyListState()
    var containerWidth by remember { mutableIntStateOf(0) }

    LaunchedEffect(currentPage, containerWidth) {
        if (totalPages > 0 && containerWidth > 0) {
            val itemWidthEstimate = 40
            val offset = (containerWidth / 2) - (itemWidthEstimate / 2)
            lazyListState.animateScrollToItem(currentPage, -offset)
        }
    }

    LazyRow(
        state = lazyListState,
        modifier = modifier.onSizeChanged { containerWidth = it.width },
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(totalPages) { page ->
            PageNumberButton(
                page = page,
                isSelected = page == currentPage,
                onClick = { onPageSelected(page) }
            )
        }
    }
}

@Composable
private fun PageNumberButton(
    page: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 4.dp),
        modifier = Modifier.defaultMinSize(minWidth = 36.dp, minHeight = 36.dp),
        colors = if (isSelected) {
            ButtonDefaults.textButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            ButtonDefaults.textButtonColors()
        }
    ) {
        Text(
            text = (page + 1).toString(),
            style = if (isSelected) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelSmall
        )
    }
}