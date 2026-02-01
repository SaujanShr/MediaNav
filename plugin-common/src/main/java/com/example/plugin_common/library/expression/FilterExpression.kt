package com.example.plugin_common.library.expression

data class FilterExpression(
    val include: Set<String> = emptySet(),
    val exclude: Set<String> = emptySet()
) : QueryExpression