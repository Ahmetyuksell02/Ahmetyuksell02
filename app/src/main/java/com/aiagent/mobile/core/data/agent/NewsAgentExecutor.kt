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
class NewsAgentExecutor @Inject constructor(
    private val api: OpenRouterApi
) : BaseAgentExecutor() {

    override val supportedTypes = setOf(AgentTaskType.NEWS_SUMMARY)

    override suspend fun execute(
        task: AgentTask,
        onProgress: suspend (Float, String) -> Unit
    ): AgentExecutionResult {
        return try {
            onProgress(0.05f, "Identifying top news stories…")

            // Step 1: Get news headlines
            val headlinesResponse = api.chatCompletion(
                ChatRequestDto(
                    model = Constants.DEFAULT_MODEL_ID,
                    messages = listOf(
                        ChatMessageDto(
                            role = "system",
                            content = "You are a news research assistant. Respond concisely with structured lists."
                        ),
                        ChatMessageDto(
                            role = "user",
                            content = "List the 5 most important and recent news stories about: \"${task.prompt}\". " +
                                "Format: numbered list with title and one-sentence context for each."
                        )
                    ),
                    maxTokens = Constants.AGENT_MAX_TOKENS
                )
            )

            val headlines = headlinesResponse.choices?.firstOrNull()?.message?.content
                ?: return AgentExecutionResult.Failure("No response from AI for headlines step")

            onProgress(0.35f, "Researching each story in depth…")

            // Step 2: Deep-dive summaries
            val summaryResponse = api.chatCompletion(
                ChatRequestDto(
                    model = Constants.DEFAULT_MODEL_ID,
                    messages = listOf(
                        ChatMessageDto(
                            role = "system",
                            content = "You are a news analyst. Provide factual, balanced summaries."
                        ),
                        ChatMessageDto(
                            role = "user",
                            content = "For each of the following news stories, write a 3-4 sentence detailed summary " +
                                "covering: key facts, main actors involved, and why it matters:\n\n$headlines"
                        )
                    ),
                    maxTokens = Constants.AGENT_MAX_TOKENS
                )
            )

            val detailedSummaries = summaryResponse.choices?.firstOrNull()?.message?.content
                ?: return AgentExecutionResult.Failure("No response from AI for summary step")

            onProgress(0.75f, "Compiling final briefing report…")

            // Step 3: Format as briefing
            val reportResponse = api.chatCompletion(
                ChatRequestDto(
                    model = Constants.DEFAULT_MODEL_ID,
                    messages = listOf(
                        ChatMessageDto(
                            role = "system",
                            content = "You are an editor. Create polished, professional daily briefings."
                        ),
                        ChatMessageDto(
                            role = "user",
                            content = "Format the following news summaries into a professional daily briefing report. " +
                                "Include a brief introduction, organized sections per story with bold headlines, " +
                                "and a 2-sentence overall outlook at the end:\n\n$detailedSummaries"
                        )
                    ),
                    maxTokens = Constants.AGENT_MAX_TOKENS
                )
            )

            val finalReport = reportResponse.choices?.firstOrNull()?.message?.content
                ?: return AgentExecutionResult.Failure("No response from AI for report formatting step")

            onProgress(1.0f, "Briefing complete")
            AgentExecutionResult.Success(finalReport)

        } catch (e: Exception) {
            Timber.e(e, "NewsAgent failed for task ${task.id}")
            AgentExecutionResult.Failure(e.message ?: "Network error during news research", isRetryable = true)
        }
    }
}
