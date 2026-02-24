package com.example.plugin_common.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class Setting(
    private val title: String,
    private val subtitle: String,
    private val leftIcon: ImageVector,
    private val onClick: (() -> Unit)? = null,
    private val dropdownContent: (@Composable () -> Unit)? = null
) {
    @Composable
    fun Content(expanded: Boolean, toggleExpanded: () -> Unit) {
        SettingContent(
            title = title,
            subtitle = subtitle,
            leftIcon = leftIcon,
            expanded = expanded,
            toggleExpanded = toggleExpanded,
            onClick = onClick,
            dropdownContent = dropdownContent
        )
    }
}

@Composable
private fun SettingContent(
    title: String,
    subtitle: String,
    leftIcon: ImageVector,
    expanded: Boolean,
    toggleExpanded: () -> Unit,
    onClick: (() -> Unit)? = null,
    dropdownContent: (@Composable () -> Unit)? = null
) {
    Column {
        SettingHeader(
            title = title,
            subtitle = subtitle,
            leftIcon = leftIcon,
            hasDropdown = dropdownContent != null,
            expanded = expanded,
            onClick = {
                if (dropdownContent != null) {
                    toggleExpanded()
                }
                onClick?.invoke()
            }
        )

        dropdownContent?.let {
            SettingDropdown(expanded, it)
        }
    }
}

@Composable
private fun SettingHeader(
    title: String,
    subtitle: String,
    leftIcon: ImageVector,
    hasDropdown: Boolean,
    expanded: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .height(72.dp)
            .fillMaxSize()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SettingHeaderContent(title, subtitle, leftIcon)

            if (hasDropdown) {
                DropdownArrow(expanded)
            }
        }
    }
}

@Composable
private fun SettingHeaderContent(
    title: String,
    subtitle: String,
    leftIcon: ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = leftIcon,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DropdownArrow(expanded: Boolean) {
    Icon(
        imageVector =
            if (expanded) Icons.Default.ArrowDropUp
            else Icons.Default.ArrowDropDown,
        contentDescription = null,
        modifier = Modifier.size(28.dp),
        tint = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun SettingDropdown(
    expanded: Boolean,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = expanded,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            content()
        }
    }
}
