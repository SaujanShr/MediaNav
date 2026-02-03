package com.example.medianav.ui.query

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.plugin_common.library.expression.SortDirection
import com.example.plugin_common.library.expression.SortExpression
import com.example.plugin_common.library.schema.field.SortFieldSchema

@Composable
fun SortField(
    fieldName: String,
    expanded: Boolean,
    schema: SortFieldSchema,
    value: SortExpression?,
    errorMessage: String? = null,
    onToggle: () -> Unit,
    onValueChange: (SortExpression) -> Unit
) {
    QueryField(
        title = fieldName,
        expanded = expanded,
        errorMessage = errorMessage,
        onToggle = onToggle
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            schema.supported.forEach { sort ->
                val isSelected = value?.sort == sort
                val arrow = when {
                    isSelected && value.direction == SortDirection.ASC -> "↑"
                    isSelected && value.direction == SortDirection.DESC -> "↓"
                    else -> ""
                }
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val newDirection = if (isSelected && value.direction == SortDirection.DESC) {
                                SortDirection.ASC
                            } else {
                                SortDirection.DESC
                            }
                            onValueChange(SortExpression(sort, newDirection))
                        },
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = sort)
                        Text(text = arrow)
                    }
                }
            }
        }
    }
}
