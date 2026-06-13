package com.ahmetyuksell.agent.concurrency

import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import timber.log.Timber
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgentConcurrencyManager @Inject constructor() {

    private val agentSemaphore = Semaphore(permits = MAX_CONCURRENT_AGENTS)
    private val streamSemaphore = Semaphore(permits = MAX_CONCURRENT_STREAMS)

    // AtomicInteger for accurate counts across concurrent coroutines
    private val activeAgentCount = AtomicInteger(0)
    private val activeStreamCount = AtomicInteger(0)

    suspend fun <T> withAgentSlot(taskId: String, block: suspend () -> T): T {
        Timber.d("Agent $taskId waiting for slot (active: ${activeAgentCount.get()}/$MAX_CONCURRENT_AGENTS)")
        return agentSemaphore.withPermit {
            val count = activeAgentCount.incrementAndGet()
            Timber.d("Agent $taskId acquired slot (active: $count)")
            try {
                block()
            } finally {
                val remaining = activeAgentCount.decrementAndGet()
                Timber.d("Agent $taskId released slot (active: $remaining)")
            }
        }
    }

    suspend fun <T> withStreamSlot(conversationId: String, block: suspend () -> T): T {
        return streamSemaphore.withPermit {
            activeStreamCount.incrementAndGet()
            try {
                block()
            } finally {
                activeStreamCount.decrementAndGet()
            }
        }
    }

    fun getActiveAgentCount(): Int = activeAgentCount.get()
    fun getActiveStreamCount(): Int = activeStreamCount.get()

    fun isAgentSlotAvailable(): Boolean = activeAgentCount.get() < MAX_CONCURRENT_AGENTS
    fun isStreamSlotAvailable(): Boolean = activeStreamCount.get() < MAX_CONCURRENT_STREAMS

    companion object {
        const val MAX_CONCURRENT_AGENTS = 2
        const val MAX_CONCURRENT_STREAMS = 3
    }
}
