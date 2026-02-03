package com.example.medianav.ui.query

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.plugin_common.library.schema.field.NumberFieldSchema

@Composable
fun NumberField(
    fieldName: String,
    expanded: Boolean,
    schema: NumberFieldSchema,
    value: Pair<Float?, Float?>?,
    errorMessage: String? = null,
    onToggle: () -> Unit,
    onValueChange: (Pair<Float?, Float?>) -> Unit
) {
    var inputFrom by remember { mutableStateOf(value?.first?.toString() ?: "") }
    var inputTo by remember { mutableStateOf(value?.second?.toString() ?: "") }

    fun filterNumberInput(input: String): String {
        return if (schema.float) {
            val filtered = input.filter { it.isDigit() || it == '.' }
            val dotCount = filtered.count { it == '.' }
            if (dotCount > 1) {
                val firstDotIndex = filtered.indexOf('.')
                filtered.filterIndexed { index, c -> c != '.' || index == firstDotIndex }
            } else filtered
        } else {
            // Only digits
            input.filter { it.isDigit() }
        }
    }

    QueryField(
        title = fieldName,
        expanded = expanded,
        errorMessage = errorMessage,
        onToggle = onToggle
    ) {
        if (schema.range) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = inputFrom,
                    onValueChange = {
                        val filtered = filterNumberInput(it)
                        inputFrom = filtered
                        onValueChange(filtered.toFloatOrNull() to inputTo.toFloatOrNull())
                    },
                    placeholder = { Text("From") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = inputTo,
                    onValueChange = {
                        val filtered = filterNumberInput(it)
                        inputTo = filtered
                        onValueChange(inputFrom.toFloatOrNull() to filtered.toFloatOrNull())
                    },
                    placeholder = { Text("To") },
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            OutlinedTextField(
                value = inputFrom,
                onValueChange = {
                    val filtered = filterNumberInput(it)
                    inputFrom = filtered
                    onValueChange(filtered.toFloatOrNull() to null)
                },
                placeholder = { Text("Enter number") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
