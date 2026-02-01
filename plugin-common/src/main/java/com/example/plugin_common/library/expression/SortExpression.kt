package com.example.plugin_common.library.expression

data class SortExpression(
    val sort: String,
    val direction: SortDirection
) : QueryExpression
