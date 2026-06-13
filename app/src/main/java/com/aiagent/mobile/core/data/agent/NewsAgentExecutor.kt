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
class NewsAgentExecutor @Inject constructor(
    private val api: OpenRouterApi
) : BaseAgentExecutor() {

    override val supportedTypes = setOf(AgentTaskType.NEWS_SUMMARY)

    override suspend fun execute(
        task: AgentTask,
        toolContexts: List<ToolResult>,
        onProgress: suspend (Float, String) -> Unit
    ): AgentExecutionResult {
        return try {
            val toolContext = toolContexts.toContextBlock()
            val systemPrompt = AgentSystemPrompts.newsAnalyst(toolContext)

            onProgress(0.1f, "Identifying top stories from live feed…")

            // Step 1: Identify and rank top stories from the live news data
            val headlinesResponse = api.chatCompletion(
                ChatRequestDto(
                    model = Constants.DEFAULT_MODEL_ID,
                    messages = listOf(
                        ChatMessageDto(role = "system", content = systemPrompt),
                        ChatMessageDto(
                            role = "user",
                            content = "From the live news data provided, identify and list the 5 most " +
                                "newsworthy stories related to: \"${task.prompt}\". " +
                                "Format: numbered list with title and one-sentence context. " +
                                "Only use stories from the data above — do not invent new ones."
                        )
                    ),
                    maxTokens = Constants.AGENT_MAX_TOKENS
                )
            )

            val headlines = headlinesResponse.choices?.firstOrNull()?.message?.content
                ?: return AgentExecutionResult.Failure("No response from AI for headlines step")

            onProgress(0.45f, "Writing in-depth summaries…")

            // Step 2: Deep summaries grounded in the tool data
            val summaryResponse = api.chatCompletion(
                ChatRequestDto(
                    model = Constants.DEFAULT_MODEL_ID,
                    messages = listOf(
                        ChatMessageDto(role = "system", content = systemPrompt),
                        ChatMessageDto(
                            role = "user",
                            content = "For each story listed below, write a 3-4 sentence summary " +
                                "covering: key facts from the live data, main actors, and why it matters. " +
                                "Cite source names where available.\n\n$headlines"
                        )
                    ),
                    maxTokens = Constants.AGENT_MAX_TOKENS
                )
            )

            val detailedSummaries = summaryResponse.choices?.firstOrNull()?.message?.content
                ?: return AgentExecutionResult.Failure("No response from AI for summary step")

            onProgress(0.80f, "Compiling final briefing…")

            // Step 3: Format as professional briefing
            val reportResponse = api.chatCompletion(
                ChatRequestDto(
                    model = Constants.DEFAULT_MODEL_ID,
                    messages = listOf(
                        ChatMessageDto(role = "system", content = systemPrompt),
                        ChatMessageDto(
                            role = "user",
                            content = "Format the following verified news summaries into a professional " +
                                "daily briefing report. Include a brief introduction, organised sections " +
                                "per story with bold headlines, and a 2-sentence overall outlook:\n\n$detailedSummaries"
                        )
                    ),
                    maxTokens = Constants.AGENT_MAX_TOKENS
                )
            )

            val finalReport = reportResponse.choices?.firstOrNull()?.message?.content
                ?: return AgentExecutionResult.Failure("No response from AI for report step")

            onProgress(1.0f, "Briefing complete")
            AgentExecutionResult.Success(finalReport)

        } catch (e: Exception) {
            Timber.e(e, "NewsAgent failed for task ${task.id}")
            AgentExecutionResult.Failure(e.message ?: "Network error during news research", isRetryable = true)
        }
    }
}
