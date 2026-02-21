package com.example.medianav.ui.library.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.FirstPage
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
internal fun PageBar(
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit
) {
    val listState = rememberLazyListState()

    // Auto-scroll to keep current page visible
    LaunchedEffect(currentPage, totalPages) {
        if (totalPages > 0) {
            val targetIndex = currentPage.coerceIn(0, totalPages - 1)
            listState.animateScrollToItem(
                index = maxOf(0, targetIndex - 2)
            )
        }
    }

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
            IconButton(
                onClick = { onPageChange(0) },
                enabled = currentPage > 0,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FirstPage,
                    contentDescription = "First page",
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(
                onClick = { onPageChange(currentPage - 1) },
                enabled = currentPage > 0,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.NavigateBefore,
                    contentDescription = "Previous page",
                    modifier = Modifier.size(20.dp)
                )
            }

            LazyRow(
                state = listState,
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(totalPages) { pageIndex ->
                    val isCurrentPage = pageIndex == currentPage

                    TextButton(
                        onClick = { onPageChange(pageIndex) },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (isCurrentPage) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        ),
                        modifier = Modifier.padding(0.dp)
                    ) {
                        Text(
                            text = "${pageIndex + 1}",
                            style = if (isCurrentPage) {
                                MaterialTheme.typography.bodyLarge
                            } else {
                                MaterialTheme.typography.bodyMedium
                            }
                        )
                    }
                }
            }

            IconButton(
                onClick = { onPageChange(currentPage + 1) },
                enabled = currentPage < totalPages - 1,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                    contentDescription = "Next page",
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(
                onClick = { onPageChange(totalPages - 1) },
                enabled = currentPage < totalPages - 1,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FirstPage,
                    contentDescription = "Last page",
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer(rotationZ = 180f)
                )
            }
        }
    }
}

