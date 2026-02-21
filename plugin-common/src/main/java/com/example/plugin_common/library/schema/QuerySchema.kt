package com.example.plugin_common.library.schema

import com.example.plugin_common.library.LibraryQuery
import com.example.plugin_common.library.expression.FilterExpression
import com.example.plugin_common.library.expression.SortExpression
import com.example.plugin_common.library.schema.field.BooleanFieldSchema
import com.example.plugin_common.library.schema.field.DateFieldSchema
import com.example.plugin_common.library.schema.field.FilterFieldSchema
import com.example.plugin_common.library.schema.field.NumberFieldSchema
import com.example.plugin_common.library.schema.field.QueryFieldSchema
import com.example.plugin_common.library.schema.field.SearchFieldSchema
import com.example.plugin_common.library.schema.field.SortFieldSchema
import java.time.LocalDate

data class QuerySchema(val fields: Map<String, QueryFieldSchema>) {
    fun defaultQuery(): LibraryQuery {
        val searchFields = mutableMapOf<String, String>()
        val booleanFields = mutableMapOf<String, Boolean>()
        val numberFields = mutableMapOf<String, Pair<Float?, Float?>>()
        val dateFields = mutableMapOf<String, Pair<LocalDate?, LocalDate?>>()
        val filterFields = mutableMapOf<String, FilterExpression>()
        val sortFields = mutableMapOf<String, SortExpression>()

        fields.forEach { (name, field) ->
            when (field) {
                is SearchFieldSchema ->
                    if (field.default != null) searchFields[name] = field.default
                is BooleanFieldSchema ->
                    if (field.default != null) booleanFields[name] = field.default
                is NumberFieldSchema ->
                    if (field.defaultTo != null && field.defaultFrom != null)
                        numberFields[name] = Pair(field.defaultTo, field.defaultFrom)
                is DateFieldSchema ->
                    if (field.defaultTo != null && field.defaultFrom != null)
                        dateFields[name] = Pair(field.defaultTo, field.defaultFrom)
                is FilterFieldSchema ->
                    if (field.defaultInclude != null && field.defaultExclude != null)
                        filterFields[name] = FilterExpression(
                            field.defaultInclude,
                            field.defaultExclude
                        )
                is SortFieldSchema ->
                    if (field.defaultSort != null && field.defaultDirection != null)
                        sortFields[name] = SortExpression(
                            field.defaultSort,
                            field.defaultDirection
                        )
            }
        }

        return LibraryQuery(
            searchFields,
            booleanFields,
            numberFields,
            dateFields,
            filterFields,
            sortFields
        )
    }
}