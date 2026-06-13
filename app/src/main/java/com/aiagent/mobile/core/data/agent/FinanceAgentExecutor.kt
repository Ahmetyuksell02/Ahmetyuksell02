package com.aiagent.mobile.core.data.agent

import com.aiagent.mobile.core.common.Constants
import com.aiagent.mobile.core.data.remote.api.OpenRouterApi
import com.aiagent.mobile.core.data.remote.dto.ChatMessageDto
import com.aiagent.mobile.core.data.remote.dto.ChatRequestDto
import com.aiagent.mobile.core.domain.agent.AgentExecutionResult
import com.aiagent.mobile.core.domain.agent.AgentSystemPrompts
import com.aiagent.mobile.core.domain.agent.BaseAgentExecutor
import com.aiagent.mobile.core.domain.model.AgentTask
import com.aiagent.mobile.core.domain.model.AgentTaskType
import com.aiagent.mobile.core.domain.tool.ToolResult
import com.aiagent.mobile.core.domain.tool.toContextBlock
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinanceAgentExecutor @Inject constructor(
    private val api: OpenRouterApi
) : BaseAgentExecutor() {

    override val supportedTypes = setOf(AgentTaskType.PRICE_MONITOR, AgentTaskType.JOB_MONITOR)

    override suspend fun execute(
        task: AgentTask,
        toolContexts: List<ToolResult>,
        onProgress: suspend (Float, String) -> Unit
    ): AgentExecutionResult {
        return try {
            val toolContext = toolContexts.toContextBlock()
            val systemPrompt = AgentSystemPrompts.financialAnalyst(toolContext)

            onProgress(0.1f, "Analysing live market data…")

            // Step 1: Contextualise the monitoring request against live data
            val contextResponse = api.chatCompletion(
                ChatRequestDto(
                    model = Constants.DEFAULT_MODEL_ID,
                    messages = listOf(
                        ChatMessageDto(role = "system", content = systemPrompt),
                        ChatMessageDto(
                            role = "user",
                            content = "Using ONLY the live market data provided in your system prompt, " +
                                "analyse this financial monitoring request: \"${task.prompt}\"\n\n" +
                                "Provide: 1) Current price/rate levels from the data, " +
                                "2) Key thresholds to watch, " +
                                "3) Trend direction based on the data, " +
                                "4) Any notable anomalies in the data. " +
                                "Label each point as DATA: or ANALYSIS: as instructed."
                        )
                    ),
                    maxTokens = Constants.AGENT_MAX_TOKENS
                )
            )

            val marketContext = contextResponse.choices?.firstOrNull()?.message?.content
                ?: return AgentExecutionResult.Failure("No response for market context analysis")

            onProgress(0.60f, "Generating risk assessment…")

            // Step 2: Actionable insights and risk assessment
            val insightsResponse = api.chatCompletion(
                ChatRequestDto(
                    model = Constants.DEFAULT_MODEL_ID,
                    messages = listOf(
                        ChatMessageDto(role = "system", content = systemPrompt),
                        ChatMessageDto(
                            role = "user",
                            content = "Based on this market analysis:\n$marketContext\n\n" +
                                "Provide: 1) Specific actionable insights, " +
                                "2) Risk assessment (LOW / MEDIUM / HIGH) with data-backed reasoning, " +
                                "3) Key indicators to monitor going forward, " +
                                "4) Short-term outlook (7–30 day horizon).\n\n" +
                                "Remember: only reference prices and rates that appear in your data. " +
                                "Include the required risk disclaimer."
                        )
                    ),
                    maxTokens = Constants.AGENT_MAX_TOKENS
                )
            )

            val insights = insightsResponse.choices?.firstOrNull()?.message?.content
                ?: return AgentExecutionResult.Failure("No response for insights step")

            onProgress(1.0f, "Financial analysis complete")

            val fullReport = "# Financial Monitor Report\n\n" +
                "**Query:** ${task.prompt}\n\n" +
                "## Market Context & Live Data\n$marketContext\n\n" +
                "## Actionable Insights & Risk Assessment\n$insights"

            AgentExecutionResult.Success(fullReport)

        } catch (e: Exception) {
            Timber.e(e, "FinanceAgent failed for task ${task.id}")
            AgentExecutionResult.Failure(e.message ?: "Network error during financial analysis", isRetryable = true)
        }
    }
}
