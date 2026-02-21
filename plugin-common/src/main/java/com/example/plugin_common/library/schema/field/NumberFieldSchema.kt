package com.example.plugin_common.library.schema.field

import com.example.plugin_common.library.schema.QueryValidationResult

data class NumberFieldSchema(
    val min: Float? = 0f,
    val max: Float? = null,
    val float: Boolean = false,
    val range: Boolean = false,
    val requiredFrom: Boolean = false,
    val requiredTo: Boolean = false,
    val defaultFrom: Float? = null,
    val defaultTo: Float? = null
): QueryFieldSchema {
    fun validate(value: Pair<Float?, Float?>?): QueryValidationResult {
        val (from, to) = value ?: (null to null)

        return if (requiredFrom && from == null) {
            QueryValidationResult(false, "From value is required")
        } else if (requiredTo && to == null) {
            QueryValidationResult(false, "To value is required")
        } else if (from != null && to != null && from > to) {
            QueryValidationResult(
                false, "From value must be less than or equal to To value"
            )
        } else {
            validateRange(from, to)
        }
    }

    private fun validateRange(from: Float?, to: Float?): QueryValidationResult =
        listOfNotNull(from, to)
            .firstNotNullOfOrNull { validateNumber(it) }
            ?: QueryValidationResult(true)

    private fun validateNumber(number: Float): QueryValidationResult? = when {
        !float && number % 1f != 0f ->
            QueryValidationResult(false, "Only integers allowed")
        min != null && number < min ->
            QueryValidationResult(false, "Minimum value is $min")
        max != null && number > max ->
            QueryValidationResult(false, "Maximum value is $max")
        else -> null
    }
}
