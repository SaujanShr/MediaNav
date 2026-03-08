package com.example.plugin_common.api

import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.math.pow

class HttpClient(requestsPerMinute: Int? = null) {
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(ApiConstants.Timeout.CONNECT_TIMEOUT_MS.toLong(), TimeUnit.MILLISECONDS)
        .readTimeout(ApiConstants.Timeout.READ_TIMEOUT_MS.toLong(), TimeUnit.MILLISECONDS)
        .followRedirects(true)
        .apply {
            if (requestsPerMinute != null) {
                addInterceptor(RateLimitInterceptor(requestsPerMinute))
            }
        }
        .build()

    suspend fun get(url: String, headers: Map<String, String> = emptyMap()): Result<String> {
        val request = buildRequest(url, headers, method = ApiConstants.Method.GET)
        return executeWithRetry { executeRequest(request) }
    }

    suspend fun post(
        url: String,
        body: String,
        headers: Map<String, String> = emptyMap()
    ): Result<String> {
        val request = buildRequest(url, headers, method = ApiConstants.Method.POST, body = body)
        return executeWithRetry { executeRequest(request) }
    }

    private suspend fun <T> executeWithRetry(
        block: suspend () -> T
    ): Result<T> {
        var lastThrowable: Throwable? = null

        for (attempt in 0 until ApiConstants.Retry.MAX_ATTEMPTS) {
            try {
                return Result.success(block())
            } catch (e: Exception) {
                lastThrowable = e
                if (!shouldRetry(e, attempt)) break

                val backoff = ApiConstants.Retry.INITIAL_BACKOFF_MS *
                        2.0.pow(attempt.toDouble()).toLong()
                delay(backoff)
            }
        }

        return Result.failure(lastThrowable ?: Exception("Unknown error"))
    }

    private fun shouldRetry(e: Exception, attempt: Int): Boolean {
        if (attempt >= ApiConstants.Retry.MAX_ATTEMPTS - 1) return false

        return when (e) {
            is IOException -> true
            is ApiException -> e.code in listOf(429, 500, 502, 503, 504)
            else -> false
        }
    }

    private fun executeRequest(request: Request): String {
        client.newCall(request).execute().use { response ->
            val code = response.code
            val body = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                throw ApiException(code, "HTTP $code: $body".trim())
            }

            return body
        }
    }

    private fun buildRequest(
        url: String,
        headers: Map<String, String>,
        method: String,
        body: String? = null
    ): Request {
        val allHeaders = ApiConstants.Request.DEFAULT_HEADERS + headers

        return Request.Builder()
            .url(url)
            .apply {
                allHeaders.forEach { (key, value) ->
                    addHeader(key, value)
                }
                when (method) {
                    ApiConstants.Method.GET -> get()
                    ApiConstants.Method.POST -> {
                        val mediaType = "application/json; charset=utf-8".toMediaType()
                        post((body ?: "").toRequestBody(mediaType))
                    }
                }
            }
            .build()
    }
}
