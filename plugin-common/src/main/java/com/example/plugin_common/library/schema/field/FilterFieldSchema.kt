package com.example.plugin_common.library.schema.field

import com.example.plugin_common.library.expression.FilterExpression
import com.example.plugin_common.library.schema.QueryValidationResult

data class FilterFieldSchema(
    val supported: Set<String> = emptySet(),
    val multiple: Boolean = false,
    val include: Boolean = false,
    val exclude: Boolean = false,
    val requiredInclude: Boolean = false,
    val requiredExclude: Boolean = false
) : QueryFieldSchema {
    fun validate(value: FilterExpression?): QueryValidationResult =
    if (value == null) validateRequired
    else validateFilter(value)

    private val validateRequired =
        if (requiredInclude || requiredExclude)
            QueryValidationResult(false, "This field is required")
        else QueryValidationResult(true)

    private fun validateFilter(expression: FilterExpression): QueryValidationResult = when {
        requiredInclude && !expression.include.isEmpty() ->
            QueryValidationResult(false, "At least one inclusion is required")
        requiredExclude && !expression.exclude.isEmpty() ->
            QueryValidationResult(false, "At least one exclusion is required")
        else -> validateSupported(expression.include + expression.exclude)
    }

    private fun validateSupported(selections: Set<String>): QueryValidationResult =
        selections.filterNot { selection ->
            selection in supported
        }.let { unsupported ->
            if (unsupported.isEmpty()) QueryValidationResult(true)
            else QueryValidationResult(
                false,
                "Unsupported selections: ${unsupported.joinToString()}"
            )
        }
}
