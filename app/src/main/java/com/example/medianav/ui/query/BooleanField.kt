package com.example.medianav.ui.query

import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable

@Composable
fun BooleanField(
    fieldName: String,
    expanded: Boolean,
    value: Boolean,
    onToggle: () -> Unit,
    onValueChange: (Boolean) -> Unit
){
    QueryField(
        title = fieldName,
        expanded = expanded,
        onToggle = onToggle
    ) {
        Switch(checked = value, onCheckedChange = onValueChange)
    }
}