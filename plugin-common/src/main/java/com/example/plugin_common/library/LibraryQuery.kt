package com.example.plugin_common.library

import com.example.plugin_common.library.expression.FilterExpression
import com.example.plugin_common.library.expression.SortExpression
import java.time.LocalDate

data class LibraryQuery(
    val searchFields: Map<String, String> = emptyMap(),
    val booleanFields: Map<String, Boolean> = emptyMap(),
    val numberFields: Map<String, Pair<Float?, Float?>> = emptyMap(),
    val dateFields: Map<String, Pair<LocalDate?, LocalDate?>> = emptyMap(),
    val filterFields: Map<String, FilterExpression> = emptyMap(),
    val sortFields: Map<String, SortExpression> = emptyMap()
)