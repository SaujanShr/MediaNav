package com.example.medianav.ui.query.input

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

object DateMask : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.take(8)

        val formatted = buildString {
            digits.forEachIndexed { index, c ->
                append(c)
                if (index == 1 || index == 3) append('/')
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                when {
                    offset <= 2 -> offset
                    offset <= 4 -> offset + 1
                    offset <= 8 -> offset + 2
                    else -> formatted.length
                }

            override fun transformedToOriginal(offset: Int): Int =
                when {
                    offset <= 2 -> offset
                    offset <= 5 -> offset - 1
                    offset <= 10 -> offset - 2
                    else -> digits.length
                }
        }

        return TransformedText(
            AnnotatedString(formatted),
            offsetMapping
        )
    }
}
