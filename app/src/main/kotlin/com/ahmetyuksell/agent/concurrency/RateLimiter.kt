package com.ahmetyuksell.agent.concurrency

import kotlinx.coroutines.delay
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RateLimiter @Inject constructor() {

    private data class Bucket(var tokens: Long, var lastRefillTime: Long)

    private val buckets = ConcurrentHashMap<String, Bucket>()

    suspend fun acquire(key: String = "default") {
        val bucket = buckets.getOrPut(key) {
            Bucket(tokens = MAX_TOKENS, lastRefillTime = System.currentTimeMillis())
        }

        var waited = 0L
        while (true) {
            val acquired = synchronized(bucket) {
                refill(bucket)
                if (bucket.tokens > 0) {
                    bucket.tokens--
                    true
                } else {
                    false
                }
            }

            if (acquired) {
                if (waited > 0) Timber.d("RateLimiter[$key]: acquired after ${waited}ms wait")
                return
            }

            Timber.w("RateLimiter[$key]: rate limit hit, waiting 1000ms")
            delay(1000L)
            waited += 1000L
        }
    }

    private fun refill(bucket: Bucket) {
        val now = System.currentTimeMillis()
        val elapsed = now - bucket.lastRefillTime
        val tokensToAdd = elapsed * MAX_TOKENS / REFILL_PERIOD_MS
        if (tokensToAdd > 0) {
            bucket.tokens = (bucket.tokens + tokensToAdd).coerceAtMost(MAX_TOKENS)
            bucket.lastRefillTime = now
        }
    }

    fun reset(key: String = "default") {
        buckets[key]?.let { synchronized(it) { it.tokens = MAX_TOKENS } }
    }

    companion object {
        private const val MAX_TOKENS = 20L
        private const val REFILL_PERIOD_MS = 60_000L
    }
}
