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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.example.medianav.ui.query.input.DateMask
import com.example.plugin_common.library.schema.field.DateFieldSchema
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Composable
fun DateField(
    fieldName: String,
    expanded: Boolean,
    schema: DateFieldSchema,
    value: Pair<LocalDate?, LocalDate?>?,
    errorMessage: String? = null,
    onToggle: () -> Unit,
    onValueChange: (Pair<LocalDate?, LocalDate?>) -> Unit
) {
    var from by remember { mutableStateOf(value?.first) }
    var to by remember { mutableStateOf(value?.second) }

    QueryField(
        title = fieldName,
        expanded = expanded,
        errorMessage = errorMessage,
        onToggle = onToggle
    ) {
        if (schema.range) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DateInput(
                    label = "From",
                    value = from,
                    modifier = Modifier.weight(1f)
                ) {
                    from = it
                    onValueChange(from to to)
                }

                DateInput(
                    label = "To",
                    value = to,
                    modifier = Modifier.weight(1f)
                ) {
                    to = it
                    onValueChange(from to to)
                }
            }
        } else {
            DateInput(
                label = "Date",
                value = from
            ) {
                from = it
                onValueChange(from to null)
            }
        }
    }
}

@Composable
private fun DateInput(
    label: String,
    value: LocalDate?,
    modifier: Modifier = Modifier,
    onChange: (LocalDate?) -> Unit
) {
    var rawDigits by remember(value) {
        mutableStateOf(
            value?.format(DateFormatter)?.filter(Char::isDigit) ?: ""
        )
    }

    OutlinedTextField(
        value = rawDigits,
        onValueChange = { newValue ->
            val digitsOnly = newValue.filter(Char::isDigit).take(8)
            rawDigits = digitsOnly

            val formatted = DateMask
                .filter(AnnotatedString(digitsOnly))
                .text
                .text

            parseMaskedDate(formatted)?.let(onChange)
            if (digitsOnly.length < 8) onChange(null)
        },
        label = { Text(label) },
        placeholder = { Text("dd/MM/yyyy") },
        visualTransformation = DateMask,
        modifier = modifier.fillMaxWidth(),
        singleLine = true
    )
}


private val DateFormatter =
    DateTimeFormatter.ofPattern("dd/MM/yyyy")

private fun parseMaskedDate(input: String): LocalDate? =
    if (input.length == 10) {
        try {
            LocalDate.parse(input, DateFormatter)
        } catch (_: DateTimeParseException) {
            null
        }
    } else null