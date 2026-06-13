package com.ahmetyuksell.agent.observability

import android.util.Log
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Phase 5 — Observability: Structured logging layer.
 * In release builds Timber is a no-op tree (via ProGuard).
 * Extend this class to add Crashlytics, Datadog, or Sentry later.
 */
@Singleton
class AppLogger @Inject constructor(
    private val metricsTracker: MetricsTracker
) {
    fun debug(tag: String, message: String) {
        Timber.tag(tag).d(message)
    }

    fun info(tag: String, message: String) {
        Timber.tag(tag).i(message)
        metricsTracker.logEvent("log_info", mapOf("tag" to tag, "message" to message.take(100)))
    }

    fun warn(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Timber.tag(tag).w(throwable, message)
        } else {
            Timber.tag(tag).w(message)
        }
        metricsTracker.logEvent("log_warn", mapOf("tag" to tag))
    }

    fun error(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Timber.tag(tag).e(throwable, message)
        } else {
            Timber.tag(tag).e(message)
        }
        metricsTracker.logEvent("log_error", mapOf("tag" to tag, "error" to message.take(200)))
    }

    fun logApiCall(endpoint: String, durationMs: Long, success: Boolean) {
        Timber.tag("API").d("$endpoint → ${durationMs}ms success=$success")
        metricsTracker.logEvent(
            "api_call",
            mapOf("endpoint" to endpoint, "duration_ms" to durationMs.toString(), "success" to success.toString())
        )
    }

    fun logAgentStep(taskId: String, step: Int, tool: String?) {
        Timber.tag("Agent").d("Task $taskId step $step tool=$tool")
        metricsTracker.logEvent("agent_step", mapOf("task_id" to taskId, "step" to step.toString()))
    }

    fun logStreamEvent(eventType: String, tokenCount: Int = 0) {
        Timber.tag("Stream").d("$eventType tokens=$tokenCount")
        metricsTracker.incrementCounter("stream_tokens", tokenCount)
    }
}
