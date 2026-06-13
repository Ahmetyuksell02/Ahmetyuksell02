package com.aiagent.mobile.core.domain.agent

import com.aiagent.mobile.core.domain.model.AgentTask
import com.aiagent.mobile.core.domain.model.AgentTaskType
import com.aiagent.mobile.core.domain.tool.ToolResult

abstract class BaseAgentExecutor {

    abstract val supportedTypes: Set<AgentTaskType>

    abstract suspend fun execute(
        task: AgentTask,
        toolContexts: List<ToolResult>,
        onProgress: suspend (progress: Float, message: String) -> Unit
    ): AgentExecutionResult

    fun supports(type: AgentTaskType): Boolean = supportedTypes.contains(type)
}
