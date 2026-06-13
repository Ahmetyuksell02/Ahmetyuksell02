package com.ahmetyuksell.agent.observability

import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLogger @Inject constructor(
    private val metricsTracker: MetricsTracker
) {
    fun debug(tag: String, message: String) {
        Timber.tag(tag).d(message)
    }

    fun info(tag: String, message: String) {
        Timber.tag(tag).i(message)
        metricsTracker.logEvent("log_info", mapOf("tag" to tag))
    }

    fun warn(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) Timber.tag(tag).w(throwable, message)
        else Timber.tag(tag).w(message)
        metricsTracker.logEvent("log_warn", mapOf("tag" to tag))
    }

    fun error(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) Timber.tag(tag).e(throwable, message)
        else Timber.tag(tag).e(message)
        // Avoid storing exception detail (may contain tokens/content) in metrics
        metricsTracker.logEvent("log_error", mapOf("tag" to tag))
    }

    fun logApiCall(endpoint: String, durationMs: Long, success: Boolean) {
        Timber.tag("API").d("$endpoint → ${durationMs}ms success=$success")
        metricsTracker.recordTiming("api_$endpoint", durationMs)
        metricsTracker.logEvent(
            if (success) "api_success" else "api_failure",
            mapOf("endpoint" to endpoint)
        )
    }

    // Called at every ReAct step for end-to-end traceability
    fun logAgentStep(taskId: String, step: Int, tool: String?, success: Boolean = true, durationMs: Long = 0) {
        Timber.tag("Agent").d("Task $taskId step $step tool=$tool success=$success")
        metricsTracker.logEvent(
            if (success) "agent_step_ok" else "agent_step_error",
            mapOf("task_id" to taskId, "step" to step.toString(), "tool" to (tool ?: "none"))
        )
        if (durationMs > 0) metricsTracker.recordTiming("tool_${tool ?: "plan"}", durationMs)
    }

    // Track agent lifecycle transitions for root cause analysis
    fun logAgentStateChange(taskId: String, fromState: String, toState: String) {
        Timber.tag("Agent").i("Task $taskId: $fromState → $toState")
        metricsTracker.logEvent("agent_state_change", mapOf("from" to fromState, "to" to toState))
    }

    // Log when streaming is interrupted so we can distinguish timeout vs network vs cancel
    fun logStreamInterrupted(conversationId: String, reason: String) {
        Timber.tag("Stream").w("Stream interrupted for conversation $conversationId: $reason")
        metricsTracker.logEvent("stream_interrupted", mapOf("reason" to reason))
    }

    fun logStreamEvent(eventType: String, tokenCount: Int = 0) {
        Timber.tag("Stream").d("$eventType tokens=$tokenCount")
        metricsTracker.incrementCounter("stream_tokens", tokenCount)
    }

    // Log boot-time recovery for monitoring crash frequency
    fun logBootRecovery(orphanedTaskCount: Int) {
        if (orphanedTaskCount > 0) {
            Timber.tag("Boot").w("Recovered $orphanedTaskCount orphaned tasks")
            metricsTracker.logEvent("boot_recovery", mapOf("orphaned" to orphanedTaskCount.toString()))
        }
    }
}
