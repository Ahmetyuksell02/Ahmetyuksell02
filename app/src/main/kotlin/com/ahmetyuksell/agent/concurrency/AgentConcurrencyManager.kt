package com.ahmetyuksell.agent.concurrency

import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Phase 5 — Concurrency Control: Limits simultaneous agent task executions
 * and streaming connections to prevent resource exhaustion and API rate limits.
 */
@Singleton
class AgentConcurrencyManager @Inject constructor() {

    private val agentSemaphore = Semaphore(permits = MAX_CONCURRENT_AGENTS)
    private val streamSemaphore = Semaphore(permits = MAX_CONCURRENT_STREAMS)

    private var activeAgents = 0
    private var activeStreams = 0

    suspend fun <T> withAgentSlot(taskId: String, block: suspend () -> T): T {
        Timber.d("Agent $taskId waiting for slot (active: $activeAgents/$MAX_CONCURRENT_AGENTS)")
        return agentSemaphore.withPermit {
            activeAgents++
            Timber.d("Agent $taskId acquired slot (active: $activeAgents)")
            try {
                block()
            } finally {
                activeAgents--
                Timber.d("Agent $taskId released slot (active: $activeAgents)")
            }
        }
    }

    suspend fun <T> withStreamSlot(conversationId: String, block: suspend () -> T): T {
        return streamSemaphore.withPermit {
            activeStreams++
            try {
                block()
            } finally {
                activeStreams--
            }
        }
    }

    fun getActiveAgentCount(): Int = activeAgents
    fun getActiveStreamCount(): Int = activeStreams

    fun isAgentSlotAvailable(): Boolean = activeAgents < MAX_CONCURRENT_AGENTS
    fun isStreamSlotAvailable(): Boolean = activeStreams < MAX_CONCURRENT_STREAMS

    companion object {
        const val MAX_CONCURRENT_AGENTS = 2
        const val MAX_CONCURRENT_STREAMS = 3
    }
}
