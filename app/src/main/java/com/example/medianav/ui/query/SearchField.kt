package com.example.medianav.ui.query

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.plugin_common.library.schema.field.SearchFieldSchema

@Composable
fun SearchField(
    fieldName: String,
    expanded: Boolean,
    schema: SearchFieldSchema,
    value: String,
    errorMessage: String? = null,
    onToggle: () -> Unit,
    onValueChange: (String) -> Unit
) {
    QueryField(
        title = fieldName,
        expanded = expanded,
        errorMessage = errorMessage,
        onToggle = onToggle
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { input ->
                val filtered = input.replace(Regex("[\\t\\n\\r]"), "")
                onValueChange(filtered)
            },
            placeholder = { Text(schema.placeholder.ifEmpty { "Enter text" }) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

