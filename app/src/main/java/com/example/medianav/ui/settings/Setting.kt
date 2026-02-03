package com.example.medianav.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
internal fun Setting(
    viewModel: SettingsViewModel,
    title: String,
    subtitle: String,
    leftIcon: ImageVector,
    type: SettingType,
    onClick: (() -> Unit)? = null,
    dropdownContent: (@Composable () -> Unit)? = null
) {
    val expandedSetting by viewModel.expandedSetting.collectAsState()
    val expanded = expandedSetting == type

    Column(modifier = Modifier.fillMaxWidth()) {
        SettingHeader(
            title = title,
            subtitle = subtitle,
            leftIcon = leftIcon,
            hasDropdown = dropdownContent != null,
            expanded = expanded,
            onClick = {
                if (dropdownContent != null) {
                    viewModel.toggleExpanded(type)
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
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
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
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = leftIcon,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(16.dp))
        Column(verticalArrangement = Arrangement.Center) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(2.dp))
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
        imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
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
