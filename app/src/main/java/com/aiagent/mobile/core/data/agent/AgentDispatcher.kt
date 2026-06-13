package com.aiagent.mobile.core.data.agent

import com.aiagent.mobile.core.domain.agent.AgentExecutionResult
import com.aiagent.mobile.core.domain.agent.BaseAgentExecutor
import com.aiagent.mobile.core.domain.model.AgentTask
import com.aiagent.mobile.core.domain.model.AgentTaskType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgentDispatcher @Inject constructor(
    private val newsExecutor: NewsAgentExecutor,
    private val financeExecutor: FinanceAgentExecutor,
    private val researchExecutor: ResearchAgentExecutor
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
        return executor.execute(task, onProgress)
    }

    fun hasExecutorFor(type: AgentTaskType): Boolean =
        executors.any { it.supports(type) }
}
