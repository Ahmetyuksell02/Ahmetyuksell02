package com.ahmetyuksell.agent.concurrency

import kotlinx.coroutines.delay
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Phase 5 — Concurrency Control: Token-bucket rate limiter for API calls.
 * Prevents hitting OpenRouter rate limits by throttling requests per minute.
 */
@Singleton
class RateLimiter @Inject constructor() {

    private data class BucketState(
        val tokens: AtomicLong,
        val lastRefillTime: AtomicLong
    )

    private val buckets = ConcurrentHashMap<String, BucketState>()

    suspend fun acquire(key: String = "default") {
        val bucket = buckets.getOrPut(key) {
            BucketState(
                tokens = AtomicLong(MAX_TOKENS),
                lastRefillTime = AtomicLong(System.currentTimeMillis())
            )
        }

        var waited = 0L
        while (true) {
            refill(bucket)

            if (bucket.tokens.get() > 0) {
                bucket.tokens.decrementAndGet()
                if (waited > 0) {
                    Timber.d("RateLimiter[$key]: acquired after ${waited}ms wait")
                }
                return
            }

            val waitMs = 1000L
            Timber.w("RateLimiter[$key]: rate limit hit, waiting ${waitMs}ms")
            delay(waitMs)
            waited += waitMs
        }
    }

    private fun refill(bucket: BucketState) {
        val now = System.currentTimeMillis()
        val elapsed = now - bucket.lastRefillTime.get()
        val tokensToAdd = (elapsed * MAX_TOKENS / REFILL_PERIOD_MS)

        if (tokensToAdd > 0) {
            val newTokens = (bucket.tokens.get() + tokensToAdd).coerceAtMost(MAX_TOKENS)
            bucket.tokens.set(newTokens)
            bucket.lastRefillTime.set(now)
        }
    }

    fun reset(key: String = "default") {
        buckets[key]?.tokens?.set(MAX_TOKENS)
    }

    companion object {
        private const val MAX_TOKENS = 20L
        private const val REFILL_PERIOD_MS = 60_000L
    }
}
