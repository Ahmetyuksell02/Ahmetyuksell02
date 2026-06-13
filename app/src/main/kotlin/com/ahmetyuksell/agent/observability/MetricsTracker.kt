package com.ahmetyuksell.agent.observability

import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Phase 5 — Observability: In-process metrics collection.
 * Tracks event counts, counters, and timing histograms in memory.
 * Can be extended to push to Firebase Analytics or a backend.
 */
@Singleton
class MetricsTracker @Inject constructor() {

    private val counters = ConcurrentHashMap<String, AtomicLong>()
    private val events = ConcurrentHashMap<String, AtomicLong>()
    private val timings = ConcurrentHashMap<String, MutableList<Long>>()

    fun logEvent(name: String, properties: Map<String, String> = emptyMap()) {
        events.getOrPut(name) { AtomicLong(0) }.incrementAndGet()
        if (properties.isNotEmpty()) {
            Timber.tag("Metrics").v("Event: $name $properties")
        }
    }

    fun incrementCounter(name: String, delta: Int = 1) {
        counters.getOrPut(name) { AtomicLong(0) }.addAndGet(delta.toLong())
    }

    fun recordTiming(operation: String, durationMs: Long) {
        timings.getOrPut(operation) { mutableListOf() }.also { list ->
            synchronized(list) {
                list.add(durationMs)
                if (list.size > 100) list.removeAt(0)
            }
        }
    }

    fun getEventCount(name: String): Long = events[name]?.get() ?: 0

    fun getCounterValue(name: String): Long = counters[name]?.get() ?: 0

    fun getAverageTiming(operation: String): Double {
        val list = timings[operation] ?: return 0.0
        return synchronized(list) {
            if (list.isEmpty()) 0.0 else list.average()
        }
    }

    fun getSummary(): String = buildString {
        appendLine("=== Metrics Summary ===")
        appendLine("Events:")
        events.entries.sortedByDescending { it.value.get() }.take(10).forEach { (k, v) ->
            appendLine("  $k: ${v.get()}")
        }
        appendLine("Counters:")
        counters.entries.forEach { (k, v) ->
            appendLine("  $k: ${v.get()}")
        }
        appendLine("Timings (avg ms):")
        timings.entries.forEach { (k, v) ->
            val avg = synchronized(v) { if (v.isEmpty()) 0.0 else v.average() }
            appendLine("  $k: ${"%.1f".format(avg)}ms")
        }
    }

    fun reset() {
        counters.clear()
        events.clear()
        timings.clear()
    }
}
