package com.example.plugin_common.api

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Interceptor
import okhttp3.Response

internal class RateLimitInterceptor(requestsPerMinute: Int) : Interceptor {
    private val mutex = Mutex()
    private var nextRequestTime = 0L
    private val minIntervalMs = (60_000.0 / requestsPerMinute).toLong()

    override fun intercept(chain: Interceptor.Chain): Response {
        runBlocking {
            mutex.withLock {
                val now = System.currentTimeMillis()
                val waitTime = nextRequestTime - now
                if (waitTime > 0) {
                    delay(waitTime)
                }
                nextRequestTime = System.currentTimeMillis() + minIntervalMs
            }
        }
        return chain.proceed(chain.request())
    }
}