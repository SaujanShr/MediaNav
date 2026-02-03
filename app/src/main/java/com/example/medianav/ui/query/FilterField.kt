package com.example.medianav.ui.query

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.plugin_common.library.expression.FilterExpression
import com.example.plugin_common.library.schema.field.FilterFieldSchema

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterField(
    fieldName: String,
    expanded: Boolean,
    schema: FilterFieldSchema,
    value: FilterExpression,
    errorMessage: String? = null,
    onToggle: () -> Unit,
    onValueChange: (FilterExpression) -> Unit
) {
    QueryField(
        title = fieldName,
        expanded = expanded,
        errorMessage = errorMessage,
        onToggle = onToggle
    ) {
        FlowRow {
            schema.supported.forEach { option ->
                val isIncluded = value.include.contains(option)
                val isExcluded = value.exclude.contains(option)

                val chipColor = when {
                    isIncluded -> MaterialTheme.colorScheme.primary
                    isExcluded -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }

                FilterChip(
                    label = { Text(option) },
                    selected = isIncluded || isExcluded,
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = if (isIncluded || isExcluded) chipColor else MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurface,
                        selectedContainerColor = chipColor,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    onClick = {
                        val newInclude = value.include.toMutableSet()
                        val newExclude = value.exclude.toMutableSet()

                        when {
                            isIncluded -> { newInclude.remove(option); newExclude.add(option) }
                            isExcluded -> newExclude.remove(option)
                            else -> newInclude.add(option)
                        }

                        onValueChange(FilterExpression(newInclude, newExclude))
                    }
                )
            }
        }
    }
}
