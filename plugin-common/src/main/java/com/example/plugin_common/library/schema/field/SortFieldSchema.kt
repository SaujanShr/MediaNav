package com.example.plugin_common.library.schema.field

import com.example.plugin_common.library.expression.SortDirection
import com.example.plugin_common.library.expression.SortExpression
import com.example.plugin_common.library.schema.QueryValidationResult

data class SortFieldSchema(
    val supported: Set<String> = emptySet(),
    val ascending: Boolean = false,
    val descending: Boolean = false,
    val required: Boolean = false
) : QueryFieldSchema {
    fun validate(value: SortExpression?): QueryValidationResult =
        if (value == null) validateRequired
        else validateSort(value)

    private val validateRequired =
        if (required) QueryValidationResult(false, "This field is required")
        else QueryValidationResult(true)

    private fun validateSort(expression: SortExpression): QueryValidationResult = when {
        expression.sort !in supported ->
            QueryValidationResult(
                false, "Sort '${expression.sort}' is not supported"
            )
        expression.direction == SortDirection.ASC && !ascending ->
            QueryValidationResult(false, "Ascending sort is not allowed")
        expression.direction == SortDirection.DESC && !descending ->
            QueryValidationResult(false, "Descending sort is not allowed")
        else -> QueryValidationResult(true)
    }
}
