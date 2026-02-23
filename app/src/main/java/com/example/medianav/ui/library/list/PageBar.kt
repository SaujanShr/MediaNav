package com.example.medianav.ui.library.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
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
import androidx.compose.ui.unit.dp

@Composable
internal fun PageBar(
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(currentPage, totalPages) {
        if (totalPages > 0) {
            val targetIndex = currentPage.coerceIn(0, totalPages - 1)
            listState.animateScrollToItem(
                index = maxOf(0, targetIndex - 2)
            )
        }
    }

    Surface(tonalElevation = 2.dp) {
        Row(
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            FirstPageButton(
                enabled = currentPage > 0,
                onClick = { onPageChange(0) }
            )

            PreviousPageButton(
                enabled = currentPage > 0,
                onClick = { onPageChange(currentPage - 1) }
            )

            PageNumberList(
                listState = listState,
                totalPages = totalPages,
                currentPage = currentPage,
                onPageChange = onPageChange,
                modifier = Modifier.weight(1f)
            )

            NextPageButton(
                enabled = currentPage < totalPages - 1,
                onClick = { onPageChange(currentPage + 1) }
            )

            LastPageButton(
                enabled = currentPage < totalPages - 1,
                onClick = { onPageChange(totalPages - 1) }
            )
        }
    }
}

@Composable
private fun FirstPageButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(40.dp)
    ) {
        Icon(
            imageVector = Icons.Default.SkipPrevious,
            contentDescription = "First page",
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun PreviousPageButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(40.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.NavigateBefore,
            contentDescription = "Previous page",
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun NextPageButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(40.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.NavigateNext,
            contentDescription = "Next page",
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun LastPageButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(40.dp)
    ) {
        Icon(
            imageVector = Icons.Default.SkipNext,
            contentDescription = "Last page",
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun PageNumberList(
    listState: androidx.compose.foundation.lazy.LazyListState,
    totalPages: Int,
    currentPage: Int,
    onPageChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        items(totalPages) { pageIndex ->
            PageNumberButton(
                pageNumber = pageIndex + 1,
                isCurrentPage = pageIndex == currentPage,
                onClick = { onPageChange(pageIndex) }
            )
        }
    }
}

@Composable
private fun PageNumberButton(
    pageNumber: Int,
    isCurrentPage: Boolean,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(
            contentColor = if (isCurrentPage) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        ),
    ) {
        Text(
            text = pageNumber.toString(),
            style = if (isCurrentPage) {
                MaterialTheme.typography.bodyLarge
            } else {
                MaterialTheme.typography.bodyMedium
            }
        )
    }
}

