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
class ResearchAgentExecutor @Inject constructor(
    private val api: OpenRouterApi
) : BaseAgentExecutor() {

    override val supportedTypes = setOf(
        AgentTaskType.RESEARCH,
        AgentTaskType.WEB_INVESTIGATION,
        AgentTaskType.FILE_ANALYSIS,
        AgentTaskType.CUSTOM
    )

    override suspend fun execute(
        task: AgentTask,
        onProgress: suspend (Float, String) -> Unit
    ): AgentExecutionResult {
        return try {
            onProgress(0.05f, "Building research outline…")

            // Step 1: Generate research outline with 4 key questions
            val outlineResponse = api.chatCompletion(
                ChatRequestDto(
                    model = Constants.DEFAULT_MODEL_ID,
                    messages = listOf(
                        ChatMessageDto(
                            role = "system",
                            content = "You are a systematic research planner. Generate precise, answerable research questions."
                        ),
                        ChatMessageDto(
                            role = "user",
                            content = "Create a research plan with exactly 4 key questions to thoroughly answer this research topic:\n" +
                                "\"${task.prompt}\"\n\n" +
                                "Format: Return ONLY the 4 questions, one per line, numbered 1-4. " +
                                "Each question should cover a distinct aspect needed for comprehensive understanding."
                        )
                    ),
                    maxTokens = 512
                )
            )

            val outlineText = outlineResponse.choices?.firstOrNull()?.message?.content
                ?: return AgentExecutionResult.Failure("Failed to generate research outline")

            // Parse questions from the outline
            val questions = outlineText.lines()
                .filter { it.trim().isNotBlank() }
                .take(4)

            if (questions.isEmpty()) {
                return AgentExecutionResult.Failure("Could not parse research questions from outline")
            }

            onProgress(0.15f, "Starting deep research on ${questions.size} questions…")

            // Step 2-N: Research each question
            val researchResults = StringBuilder()
            val progressPerQuestion = 0.55f / questions.size

            questions.forEachIndexed { index, question ->
                val currentProgress = 0.15f + (index * progressPerQuestion)
                onProgress(currentProgress, "Researching: ${question.take(60)}…")

                val questionResponse = api.chatCompletion(
                    ChatRequestDto(
                        model = Constants.DEFAULT_MODEL_ID,
                        messages = listOf(
                            ChatMessageDto(
                                role = "system",
                                content = "You are a domain expert. Provide thorough, accurate, and well-cited analysis."
                            ),
                            ChatMessageDto(
                                role = "user",
                                content = "Research context: \"${task.prompt}\"\n\n" +
                                    "Answer this specific question in depth (4-6 paragraphs): $question\n\n" +
                                    "Include specific examples, data points, and key considerations."
                            )
                        ),
                        maxTokens = Constants.AGENT_MAX_TOKENS
                    )
                )

                val answer = questionResponse.choices?.firstOrNull()?.message?.content ?: ""
                researchResults.append("## $question\n\n$answer\n\n")
            }

            onProgress(0.80f, "Synthesizing final report…")

            // Step final: Synthesize everything into a comprehensive report
            val synthesisResponse = api.chatCompletion(
                ChatRequestDto(
                    model = Constants.DEFAULT_MODEL_ID,
                    messages = listOf(
                        ChatMessageDto(
                            role = "system",
                            content = "You are an expert technical writer. Synthesize complex research into clear, " +
                                "comprehensive reports with executive summaries and actionable conclusions."
                        ),
                        ChatMessageDto(
                            role = "user",
                            content = "Research topic: \"${task.prompt}\"\n\n" +
                                "Based on the following research findings, create a comprehensive, well-structured report:\n\n" +
                                researchResults.toString() +
                                "\n\nReport structure: Executive Summary (3-4 sentences), " +
                                "Key Findings (organized sections from the research), " +
                                "Conclusions & Recommendations, Further Research Areas."
                        )
                    ),
                    maxTokens = Constants.AGENT_MAX_TOKENS
                )
            )

            val synthesis = synthesisResponse.choices?.firstOrNull()?.message?.content
                ?: return AgentExecutionResult.Failure("Failed to synthesize research report")

            onProgress(1.0f, "Research complete")
            AgentExecutionResult.Success(synthesis)

        } catch (e: Exception) {
            Timber.e(e, "ResearchAgent failed for task ${task.id}")
            AgentExecutionResult.Failure(e.message ?: "Network error during research", isRetryable = true)
        }
    }
}
