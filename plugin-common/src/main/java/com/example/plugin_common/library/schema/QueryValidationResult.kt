package com.example.plugin_common.library.schema

data class QueryValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)