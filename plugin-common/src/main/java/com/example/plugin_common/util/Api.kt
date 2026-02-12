package com.example.plugin_common.util

import com.google.gson.annotations.SerializedName
import java.net.URLEncoder
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

fun buildApiUrl(
    baseUrl: String,
    pathParams: Any? = null,
    queryParams: Any? = null
): String {
    val urlWithPathParams = replacePathParams(baseUrl, pathParams)
    val urlWithQueryParams = appendQueryParams(urlWithPathParams, queryParams)

    return urlWithQueryParams
}

private fun replacePathParams(url: String, pathParams: Any?): String {
    if (pathParams == null) return url

    var result = url

    pathParams::class.memberProperties.forEach { property ->
        val value = property.getter.call(pathParams) ?: return@forEach
        val paramName = getSerializedName(property)
        val paramValue = getStringValue(value)
        val encodedValue = URLEncoder.encode(paramValue, Charsets.UTF_8)

        result = result.replace("{$paramName}", encodedValue)
    }

    return result
}

private fun appendQueryParams(url: String, queryParams: Any?): String {
    if (queryParams == null) return url

    val params = queryParams::class.memberProperties
        .mapNotNull { property ->
            buildQueryParam(property, queryParams)
        }

    if (params.isEmpty()) return url

    return "$url?${params.joinToString("&")}"
}

private fun buildQueryParam(
    property: KProperty1<out Any, Any?>,
    owner: Any
): String? {
    val value = property.getter.call(owner) ?: return null

    val key = getSerializedName(property)
    val stringValue = getStringValue(value)

    val encodedKey = URLEncoder.encode(key, Charsets.UTF_8)
    val encodedValue = URLEncoder.encode(stringValue, Charsets.UTF_8)

    return "$encodedKey=$encodedValue"
}

private fun getSerializedName(property: KProperty1<out Any, *>): String {
    return property.findAnnotation<SerializedName>()?.value ?: property.name
}

private fun getStringValue(value: Any): String {
    return when (value) {
        is Enum<*> -> getEnumSerializedName(value)
        else -> value.toString()
    }
}

private fun getEnumSerializedName(enum: Enum<*>): String {
    val field = enum.javaClass.getField(enum.name)
    return field.getAnnotation(SerializedName::class.java)?.value ?: enum.name
}