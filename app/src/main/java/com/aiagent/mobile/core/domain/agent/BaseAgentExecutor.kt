package com.aiagent.mobile.core.domain.agent

import com.aiagent.mobile.core.domain.model.AgentTask
import com.aiagent.mobile.core.domain.model.AgentTaskType

abstract class BaseAgentExecutor {

    abstract val supportedTypes: Set<AgentTaskType>

    abstract suspend fun execute(
        task: AgentTask,
        onProgress: suspend (progress: Float, message: String) -> Unit
    ): AgentExecutionResult

    fun supports(type: AgentTaskType): Boolean = supportedTypes.contains(type)
}
