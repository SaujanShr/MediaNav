package com.example.medianav.ui.query

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.plugin_common.library.LibraryQuery
import com.example.plugin_common.library.expression.FilterExpression
import com.example.plugin_common.library.expression.SortExpression
import com.example.plugin_common.library.schema.QuerySchema
import com.example.plugin_common.library.schema.field.DateFieldSchema
import com.example.plugin_common.library.schema.field.FilterFieldSchema
import com.example.plugin_common.library.schema.field.NumberFieldSchema
import com.example.plugin_common.library.schema.field.SearchFieldSchema
import com.example.plugin_common.library.schema.field.SortFieldSchema
import java.time.LocalDate

@Composable
fun QueryForm(
    schema: QuerySchema,
    initialQuery: LibraryQuery = LibraryQuery(),
    onQueryChange: (LibraryQuery) -> Unit,
    onDismiss: () -> Unit,
    onApply: (LibraryQuery) -> Unit
) {
    var queryState by remember { mutableStateOf(initialQuery) }
    var expandedField by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text("Create Query", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        QueryFields(
            schema = schema,
            queryState = queryState,
            expandedField = expandedField,
            onFieldToggle = { field ->
                expandedField = if (expandedField == field) null else field
            },
            onValueChange = { field, value ->
                queryState = queryState.copyWith(field, value)
                onQueryChange(queryState)
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onDismiss) { Text("Cancel") }
            Spacer(Modifier.width(8.dp))
            Button(onClick = { onApply(queryState) }) { Text("Apply") }
        }
    }
}


@Composable
fun QueryFields(
    schema: QuerySchema,
    queryState: LibraryQuery,
    expandedField: String?,
    onFieldToggle: (String) -> Unit,
    onValueChange: (fieldName: String, value: Any?) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        schema.fields.forEach { (fieldName, fieldSchema) ->
            val isExpanded = expandedField == fieldName

            when (fieldSchema) {
                is SearchFieldSchema -> {
                    val currentValue = queryState.searchFields[fieldName] ?: ""
                    val validation = fieldSchema.validate(currentValue)
                    SearchField(
                        fieldName = fieldName,
                        expanded = isExpanded,
                        schema = fieldSchema,
                        value = currentValue,
                        errorMessage = validation.errorMessage.takeIf { !validation.isValid },
                        onToggle = { onFieldToggle(fieldName) },
                        onValueChange = { newValue -> onValueChange(fieldName, newValue) }
                    )
                }

                is NumberFieldSchema -> {
                    val currentValue = queryState.numberFields[fieldName]
                    val validation = fieldSchema.validate(currentValue)
                    NumberField(
                        fieldName = fieldName,
                        expanded = isExpanded,
                        schema = fieldSchema,
                        value = currentValue,
                        errorMessage = validation.errorMessage.takeIf { !validation.isValid },
                        onToggle = { onFieldToggle(fieldName) },
                        onValueChange = { newValue -> onValueChange(fieldName, newValue) }
                    )
                }

                is DateFieldSchema -> {
                    val currentValue = queryState.dateFields[fieldName]
                    val validation = fieldSchema.validate(currentValue)
                    DateField(
                        fieldName = fieldName,
                        expanded = isExpanded,
                        schema = fieldSchema,
                        value = currentValue,
                        errorMessage = validation.errorMessage.takeIf { !validation.isValid },
                        onToggle = { onFieldToggle(fieldName) },
                        onValueChange = { newValue -> onValueChange(fieldName, newValue) }
                    )
                }

                is FilterFieldSchema -> {
                    val currentValue = queryState.filterFields[fieldName] ?: FilterExpression()
                    val validation = fieldSchema.validate(currentValue)
                    FilterField(
                        fieldName = fieldName,
                        expanded = isExpanded,
                        schema = fieldSchema,
                        value = currentValue,
                        errorMessage = validation.errorMessage.takeIf { !validation.isValid },
                        onToggle = { onFieldToggle(fieldName) },
                        onValueChange = { newValue -> onValueChange(fieldName, newValue) }
                    )
                }

                is SortFieldSchema -> {
                    val currentValue = queryState.sortFields[fieldName]
                    val validation = fieldSchema.validate(currentValue)
                    SortField(
                        fieldName = fieldName,
                        expanded = isExpanded,
                        schema = fieldSchema,
                        value = currentValue,
                        errorMessage = validation.errorMessage.takeIf { !validation.isValid },
                        onToggle = { onFieldToggle(fieldName) },
                        onValueChange = { newValue -> onValueChange(fieldName, newValue) }
                    )
                }
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
private fun LibraryQuery.copyWith(fieldName: String, value: Any?): LibraryQuery {
    return when (fieldName) {
        in searchFields -> copy(
            searchFields = searchFields + (fieldName to value as String)
        )
        in numberFields -> copy(
            numberFields = numberFields + (fieldName to value as Pair<Float?, Float?>)
        )
        in dateFields -> copy(
            dateFields = dateFields + (fieldName to value as Pair<LocalDate?, LocalDate?>)
        )
        in filterFields -> copy(
            filterFields = filterFields + (fieldName to value as FilterExpression)
        )
        in sortFields -> copy(
            sortFields = sortFields + (fieldName to value as SortExpression)
        )
        else -> when (value) {
            is FilterExpression -> copy(filterFields = filterFields + (fieldName to value))
            is SortExpression -> copy(sortFields = sortFields + (fieldName to value))
            else -> this
        }
    }
}