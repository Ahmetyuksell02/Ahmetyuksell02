package com.aiagent.mobile.core.data.agent

import com.aiagent.mobile.core.common.Constants
import com.aiagent.mobile.core.data.remote.api.OpenRouterApi
import com.aiagent.mobile.core.data.remote.dto.ChatMessageDto
import com.aiagent.mobile.core.data.remote.dto.ChatRequestDto
import com.aiagent.mobile.core.domain.agent.AgentExecutionResult
import com.aiagent.mobile.core.domain.agent.BaseAgentExecutor
import com.aiagent.mobile.core.domain.model.AgentTask
import com.aiagent.mobile.core.domain.model.AgentTaskType
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
        onProgress: suspend (Float, String) -> Unit
    ): AgentExecutionResult {
        return try {
            onProgress(0.05f, "Analyzing financial context…")

            // Step 1: Parse and contextualize the monitoring request
            val contextResponse = api.chatCompletion(
                ChatRequestDto(
                    model = Constants.DEFAULT_MODEL_ID,
                    messages = listOf(
                        ChatMessageDto(
                            role = "system",
                            content = "You are a professional financial analyst with deep market knowledge. " +
                                "Provide structured, data-driven analysis. Note: you don't have real-time data, " +
                                "but can provide expert analysis and context based on your training knowledge."
                        ),
                        ChatMessageDto(
                            role = "user",
                            content = "Analyze this financial monitoring request and provide current market context: " +
                                "\"${task.prompt}\"\n\n" +
                                "Include: 1) Current market context, 2) Key price levels or thresholds to watch, " +
                                "3) Recent trend analysis, 4) Key factors affecting this asset/topic."
                        )
                    ),
                    maxTokens = Constants.AGENT_MAX_TOKENS
                )
            )

            val marketContext = contextResponse.choices?.firstOrNull()?.message?.content
                ?: return AgentExecutionResult.Failure("No response for market context analysis")

            onProgress(0.55f, "Generating actionable insights…")

            // Step 2: Actionable insights and risk assessment
            val insightsResponse = api.chatCompletion(
                ChatRequestDto(
                    model = Constants.DEFAULT_MODEL_ID,
                    messages = listOf(
                        ChatMessageDto(
                            role = "system",
                            content = "You are a financial risk analyst. Be specific, data-oriented, and practical."
                        ),
                        ChatMessageDto(
                            role = "user",
                            content = "Based on this analysis:\n$marketContext\n\n" +
                                "Provide: 1) Specific actionable insights, " +
                                "2) Risk assessment (low/medium/high with reasoning), " +
                                "3) Key indicators to monitor going forward, " +
                                "4) Brief outlook for the next 7-30 days."
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
                "## Market Context\n$marketContext\n\n" +
                "## Actionable Insights & Risk Assessment\n$insights"

            AgentExecutionResult.Success(fullReport)

        } catch (e: Exception) {
            Timber.e(e, "FinanceAgent failed for task ${task.id}")
            AgentExecutionResult.Failure(e.message ?: "Network error during financial analysis", isRetryable = true)
        }
    }
}
