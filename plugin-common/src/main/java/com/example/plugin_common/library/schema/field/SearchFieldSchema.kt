package com.example.plugin_common.library.schema.field

import com.example.plugin_common.library.schema.QueryValidationResult

data class SearchFieldSchema(
    val placeholder: String,
    val required: Boolean = false,
    val minLength: Int? = null,
    val maxLength: Int? = null
): QueryFieldSchema {
    fun validate(value: String?): QueryValidationResult =
        if (value.isNullOrBlank()) validateRequired
        else validateSearch(value)

    private val validateRequired =
        if (required) QueryValidationResult(false, "This field is required")
        else QueryValidationResult(true)

    private fun validateSearch(search: String): QueryValidationResult = when {
        minLength != null && search.length < minLength ->
            QueryValidationResult(false, "Minimum length is $minLength")
        maxLength != null && search.length > maxLength ->
            QueryValidationResult(false, "Maximum length is $maxLength")
        else -> QueryValidationResult(true)
    }
}