package com.example.medianav.ui.shared

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.medianav.ui.query.QueryForm
import com.example.plugin_common.library.LibraryQuery
import com.example.plugin_common.library.schema.QuerySchema

@Composable
fun LibraryQueryDialog(
    schema: QuerySchema,
    onDismiss: () -> Unit,
    onApply: (LibraryQuery) -> Unit
) {
    var queryState by remember { mutableStateOf(LibraryQuery()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            QueryForm(
                schema = schema,
                initialQuery = queryState,
                onDismiss = onDismiss,
                onApply = onApply,
                onQueryChange = { queryState = it }
            )
        }
    }
}
