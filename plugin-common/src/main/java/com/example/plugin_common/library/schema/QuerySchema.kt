package com.example.plugin_common.library.schema

import com.example.plugin_common.library.schema.field.QueryFieldSchema

data class QuerySchema(
    val fields: Map<String, QueryFieldSchema>
)