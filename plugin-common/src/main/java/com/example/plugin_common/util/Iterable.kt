package com.example.plugin_common.util

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

suspend fun <A, B> Iterable<A>.mapAsync(
    transform: suspend (A) -> B
): List<B> = coroutineScope {
    map { async { transform(it) } }.awaitAll()
}

suspend fun <A, B : Any> Iterable<A>.mapAsyncNotNull(
    transform: suspend (A) -> B?
): List<B> = coroutineScope {
    map { async { transform(it) } }
        .awaitAll()
        .filterNotNull()
}

inline fun <T, R> Iterable<T>.firstOrDefault(
    default: R,
    transform: (T) -> R?
): R {
    for (element in this) {
        val value = transform(element)
        if (value != null) return value
    }
    return default
}