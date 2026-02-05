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
    private var lastRequestTime = 0L

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
                
                val backoff = ApiConstants.Retry.INITIAL_BACKOFF_MS * 2.0.pow(attempt).toLong()
                delay(backoff)
            }
        }
        return Result.failure(lastThrowable ?: Exception("Unknown error"))
    }

    private fun shouldRetry(e: Exception, attempt: Int): Boolean {
        if (attempt >= ApiConstants.Retry.MAX_ATTEMPTS - 1) return false

        return when (e) {
            is IOException -> true
            is ApiException -> e.code in listOf(500, 502, 503, 504)
            else -> false
        }
    }

    private suspend fun applyRateLimit(requestsPerMinute: Int?) = lock.withLock {
        if (requestsPerMinute == null || requestsPerMinute <= 0) return@withLock

        val windowMs = 60_000L / requestsPerMinute
        val now = System.currentTimeMillis()
        val elapsed = now - lastRequestTime
        val waitTime = windowMs - elapsed

        if (waitTime > 0) {
            delay(waitTime)
        }
        lastRequestTime = System.currentTimeMillis()
    }

    private fun readResponse(conn: HttpURLConnection): String {
        val code = conn.responseCode
        val responseStream = if (code in 200..299) conn.inputStream else conn.errorStream

        if (responseStream == null) {
            if (code !in 200..299) {
                throw ApiException(code, "HTTP $code")
            }
            return ""
        }

        val stream = if (
            ApiConstants.Response.GZIP
                .equals(conn.contentEncoding, ignoreCase = true)
            ) {
            GZIPInputStream(responseStream)
        } else {
            responseStream
        }

        val responseText = stream.bufferedReader().use { it.readText() }

        if (code !in 200..299) {
            throw ApiException(code, "HTTP $code $responseText".trim())
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

        val allHeaders = ApiConstants.Request.DEFAULT_HEADERS + headers
        allHeaders.forEach { (key, value) ->
            connection.setRequestProperty(key, value)
        }

        return connection
    }
}
