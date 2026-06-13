package com.ahmetyuksell.agent.domain.agent

import com.ahmetyuksell.agent.domain.model.AgentTask
import kotlinx.serialization.Serializable

interface AgentOrchestrator {
    suspend fun execute(
        task: AgentTask,
        onProgress: (step: Int, thought: String) -> Unit
    ): AgentResult

    suspend fun cancel(taskId: String)
}

sealed class AgentResult {
    data class Success(
        val answer: String,
        val steps: List<AgentStep>
    ) : AgentResult()

    data class Failed(
        val error: String,
        val steps: List<AgentStep>
    ) : AgentResult()

    data object Cancelled : AgentResult()
}

@Serializable
data class AgentStep(
    val stepNumber: Int,
    val thought: String,
    val action: String? = null,
    val actionInput: String? = null,
    val observation: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
