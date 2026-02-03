package com.example.medianav.ui.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.plugin_common.library.LibraryItem
import com.example.plugin_common.library.LibraryItemStatus
import com.example.plugin_common.plugin.MediaPlugin

@Composable
internal fun LibraryItemCard(
    viewModel: LibraryViewModel,
    item: LibraryItem,
    plugin: MediaPlugin
) {
    val expandedItemId by viewModel.expandedItemId.collectAsState()
    val expanded = expandedItemId == item.id

    val cardColor = when (item.status) {
        LibraryItemStatus.LIKED -> MaterialTheme.colorScheme.tertiaryContainer
        LibraryItemStatus.VIEWED -> MaterialTheme.colorScheme.secondaryContainer
        LibraryItemStatus.NONE -> MaterialTheme.colorScheme.surface
    }

    val contentColor = when (item.status) {
        LibraryItemStatus.LIKED -> MaterialTheme.colorScheme.onTertiaryContainer
        LibraryItemStatus.VIEWED -> MaterialTheme.colorScheme.onSecondaryContainer
        LibraryItemStatus.NONE -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = cardColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            ItemTitle(item, contentColor)

            Spacer(modifier = Modifier.height(12.dp))

            ItemContent(plugin, item)

            Spacer(modifier = Modifier.height(12.dp))

            ActionBar(
                viewModel = viewModel,
                item = item,
                expanded = expanded,
                onToggleExpand = { viewModel.toggleExpanded(item.id) }
            )

            ItemDropdown(plugin, item, expanded)
        }
    }
}

@Composable
private fun ItemTitle(
    item: LibraryItem,
    contentColor: androidx.compose.ui.graphics.Color
)  {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = item.title,
            style = MaterialTheme.typography.titleMedium,
            color = contentColor
        )
        item.subtitle?.let { subtitle ->
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun ItemContent(plugin: MediaPlugin, item: LibraryItem) {
    Row(verticalAlignment = Alignment.Top) {
        Thumbnail(item)

        Spacer(modifier = Modifier.width(12.dp))

        plugin.DetailsContent(item)
    }
}

@Composable
private fun ActionBar(
    viewModel: LibraryViewModel,
    item: LibraryItem,
    expanded: Boolean,
    onToggleExpand: () -> Unit
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        IconButton(onClick = { viewModel.toggleViewItem(context, item) }) {
            Icon(
                imageVector =
                    if (item.status == LibraryItemStatus.VIEWED) Icons.Default.VisibilityOff
                    else Icons.Default.Visibility,
                contentDescription = null
            )
        }

        IconButton(onClick = { viewModel.toggleLikeItem(context, item) }) {
            Icon(
                imageVector =
                    if (item.status == LibraryItemStatus.LIKED) Icons.Default.HeartBroken
                    else Icons.Default.Favorite,
                contentDescription = null
            )
        }

        IconButton(onClick = onToggleExpand) {
            Icon(
                imageVector =
                    if (expanded) Icons.Default.ArrowDropUp
                    else Icons.Default.ArrowDropDown,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun ItemDropdown(
    plugin: MediaPlugin,
    item: LibraryItem,
    expanded: Boolean
) {
    AnimatedVisibility(
        visible = expanded,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 300.dp, max = 600.dp)
                .padding(8.dp)
        ) {
            plugin.DropdownContent(item)
        }
    }
}
