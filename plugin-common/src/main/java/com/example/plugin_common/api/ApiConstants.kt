package com.example.plugin_common.api

object ApiConstants {
    object Method {
        const val GET = "GET"
    }

    object Timeout {
        const val CONNECT_TIMEOUT_MS = 15_000
        const val READ_TIMEOUT_MS = 15_000
    }

    object Header {
        const val ACCEPT = "Accept"
        const val ACCEPT_ENCODING = "Accept-Encoding"
        const val ACCEPT_LANGUAGE = "Accept-Language"
        const val CACHE_CONTROL = "Cache-Control"
        const val CONNECTION = "Connection"
        const val USER_AGENT = "User-Agent"
    }

    object Request {
        val DEFAULT_HEADERS = mapOf(
            Header.ACCEPT to "application/json, application/xml, text/xml, text/html, */*;q=0.8",
            Header.ACCEPT_ENCODING to "gzip, deflate, br",
            Header.ACCEPT_LANGUAGE to "en-US,en;q=0.9,*;q=0.8",
            Header.CACHE_CONTROL to "no-cache",
            Header.CONNECTION to "keep-alive",
            Header.USER_AGENT to "Medianav/1.0 (Android)"
        )
    }
}