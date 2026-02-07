package com.example.plugin_common.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.GZIPInputStream
import kotlin.math.pow

class Api(val requestsPerMinute: Int? = null) {
    private val lock = Mutex()
    private var nextRequestTime = 0L

    suspend fun get(url: String, headers: Map<String, String> = emptyMap()) =
        withContext(Dispatchers.IO) {
            executeWithRetry(requestsPerMinute) {
                val conn = buildConnection(url, headers)
                try {
                    conn.connect()
                    readResponse(conn)
                } finally {
                    conn.disconnect()
                }
            }
        }

    private suspend fun <T> executeWithRetry(
        requestsPerMinute: Int?,
        block: suspend () -> T
    ): Result<T> {
        var lastThrowable: Throwable? = null

        for (attempt in 0 until ApiConstants.Retry.MAX_ATTEMPTS) {
            try {
                applyRateLimit(requestsPerMinute)
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

    private suspend fun applyRateLimit(requestsPerMinute: Int?) {
        if (requestsPerMinute == null || requestsPerMinute <= 0) return

        val windowMs = 60_000L / requestsPerMinute
        val waitTime = lock.withLock {
            val now = System.currentTimeMillis()
            val scheduledTime = maxOf(nextRequestTime, now)
            nextRequestTime = scheduledTime + windowMs
            scheduledTime - now
        }

        if (waitTime > 0) {
            delay(waitTime)
        }
    }

    private fun readResponse(conn: HttpURLConnection): String {
        val code = conn.responseCode
        val isSuccess = code in 200..299
        val responseStream = if (isSuccess) conn.inputStream else conn.errorStream

        val responseText = responseStream?.let { stream ->
            val wrappedStream =
                if (ApiConstants.Response.GZIP.equals(
                        conn.contentEncoding, ignoreCase = true
                )) {
                    GZIPInputStream(stream)
                } else {
                    stream
                }
            wrappedStream.bufferedReader().use { it.readText() }
        } ?: ""

        if (!isSuccess) {
            throw ApiException(code, "HTTP $code: $responseText".trim())
        }

        return responseText
    }

    private fun buildConnection(
        url: String,
        headers: Map<String, String>
    ): HttpURLConnection {
        val connection = URL(url).openConnection() as HttpURLConnection

        connection.requestMethod = ApiConstants.Method.GET
        connection.connectTimeout = ApiConstants.Timeout.CONNECT_TIMEOUT_MS
        connection.readTimeout = ApiConstants.Timeout.READ_TIMEOUT_MS
        connection.instanceFollowRedirects = true

        val allHeaders = ApiConstants.Request.DEFAULT_HEADERS + headers
        allHeaders.forEach { (key, value) ->
            connection.setRequestProperty(key, value)
        }

        return connection
    }
}
