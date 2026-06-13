package com.aiagent.mobile.core.data.agent

import com.aiagent.mobile.core.data.tool.ToolRegistry
import com.aiagent.mobile.core.domain.agent.AgentExecutionResult
import com.aiagent.mobile.core.domain.agent.BaseAgentExecutor
import com.aiagent.mobile.core.domain.model.AgentTask
import com.aiagent.mobile.core.domain.model.AgentTaskType
import com.aiagent.mobile.core.domain.tool.ToolResult
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgentDispatcher @Inject constructor(
    private val newsExecutor: NewsAgentExecutor,
    private val financeExecutor: FinanceAgentExecutor,
    private val researchExecutor: ResearchAgentExecutor,
    private val toolRegistry: ToolRegistry
) {
    private val executors: List<BaseAgentExecutor> = listOf(
        newsExecutor, financeExecutor, researchExecutor
    )

    suspend fun dispatch(
        task: AgentTask,
        onProgress: suspend (Float, String) -> Unit
    ): AgentExecutionResult {
        val executor = executors.firstOrNull { it.supports(task.taskType) }
            ?: return AgentExecutionResult.Failure(
                "No executor registered for task type: ${task.taskType}",
                isRetryable = false
            )

        // Phase 1: Select and execute tools
        onProgress(0.05f, "Selecting tools…")
        val tools = toolRegistry.selectTools(task)
        Timber.d("AgentDispatcher: ${tools.size} tool(s) selected for ${task.taskType}")

        val toolResults = mutableListOf<ToolResult>()
        tools.forEachIndexed { i, tool ->
            val toolProgress = 0.05f + (i + 1) * 0.25f / tools.size.coerceAtLeast(1)
            onProgress(toolProgress, "Fetching ${tool.toolType.displayName}…")
            val result = tool.execute(task)
            toolResults.add(result)
            if (result is ToolResult.Failure && result.isFatal) {
                Timber.w("Tool ${tool.toolType} returned fatal failure: ${result.error}")
                return AgentExecutionResult.Failure(
                    "Tool ${tool.toolType.displayName} failed: ${result.error}",
                    isRetryable = true
                )
            }
        }

        // Phase 2: Hand off to executor with real tool context
        onProgress(0.35f, "Analyzing with AI…")
        return executor.execute(task, toolResults) { p, m ->
            onProgress(0.35f + p * 0.65f, m)
        }
    }

    fun hasExecutorFor(type: AgentTaskType): Boolean =
        executors.any { it.supports(type) }
}
