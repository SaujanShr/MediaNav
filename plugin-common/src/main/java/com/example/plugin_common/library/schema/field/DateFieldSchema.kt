package com.example.plugin_common.library.schema.field

import com.example.plugin_common.library.schema.QueryValidationResult
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class DateFieldSchema(
    val min: LocalDate? = null,
    val max: LocalDate? = null,
    val range: Boolean = false,
    val requiredFrom: Boolean = false,
    val requiredTo: Boolean = false
): QueryFieldSchema {
    private val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    private val minimumDate = min?.format(formatter)
    private val maximumDate = max?.format(formatter)



    fun validate(value: Pair<LocalDate?, LocalDate?>?): QueryValidationResult {
        val (from, to) = value ?: (null to null)

        return if (requiredFrom && from == null) {
            QueryValidationResult(false, "From date is required")
        } else if (requiredTo && to == null) {
            QueryValidationResult(false, "To date is required")
        } else if (from != null && to != null && from > to) {
            QueryValidationResult(
                false, "From date must be earlier than or equal to To date"
            )
        } else {
            validateRange(from, to)
        }
    }

    private fun validateRange(from: LocalDate?, to: LocalDate?): QueryValidationResult =
        listOfNotNull(from, to)
            .firstNotNullOfOrNull { validateDate(it) }
            ?: QueryValidationResult(true)

    private fun validateDate(date: LocalDate): QueryValidationResult? = when {
        min != null && date.isBefore(min) ->
            QueryValidationResult(false, "Minimum date is $minimumDate")
        max != null && date.isAfter(max) ->
            QueryValidationResult(false, "Maximum date is $maximumDate")
        else -> null
    }
}
