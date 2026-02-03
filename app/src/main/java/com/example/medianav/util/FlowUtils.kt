package com.example.medianav.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

suspend fun <T> Flow<T>.firstResult(): Result<T> =
    map { Result.success(it) }
        .catch { e -> emit(Result.failure(e)) }
        .first()