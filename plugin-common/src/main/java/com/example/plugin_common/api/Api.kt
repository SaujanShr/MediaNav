package com.example.plugin_common.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object Api {
    suspend fun get(
        url: String,
        headers: Map<String, String> = emptyMap()
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val conn = buildConnection(url, headers)

            try {
                conn.connect()
                readResponse(conn)
            } finally {
                conn.disconnect()
            }
        }
    }

    private fun readResponse(conn: HttpURLConnection): String {
        val code = conn.responseCode
        val stream = if (code in 200..299) {
            conn.inputStream
        } else {
            val error = conn.errorStream
                ?.bufferedReader()
                ?.use { it.readText() }

            throw Exception("HTTP $code ${error ?: ""}".trim())
        }

        return stream.bufferedReader().use { it.readText() }
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