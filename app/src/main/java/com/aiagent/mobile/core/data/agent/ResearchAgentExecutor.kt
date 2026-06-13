package com.aiagent.mobile.core.data.agent

import com.aiagent.mobile.core.common.Constants
import com.aiagent.mobile.core.data.local.dao.AgentExecutionLogDao
import com.aiagent.mobile.core.data.local.entity.AgentExecutionLogEntity
import com.aiagent.mobile.core.data.local.entity.LogStepType
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
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResearchAgentExecutor @Inject constructor(
    private val api: OpenRouterApi,
    private val executionLogDao: AgentExecutionLogDao
) : BaseAgentExecutor() {

    override val supportedTypes = setOf(
        AgentTaskType.RESEARCH,
        AgentTaskType.WEB_INVESTIGATION,
        AgentTaskType.FILE_ANALYSIS,
        AgentTaskType.CUSTOM
    )

    override suspend fun execute(
        task: AgentTask,
        toolContexts: List<ToolResult>,
        onProgress: suspend (Float, String) -> Unit
    ): AgentExecutionResult {
        return try {
            val toolContext = toolContexts.toContextBlock()
            val systemPrompt = when (task.taskType) {
                AgentTaskType.FILE_ANALYSIS -> AgentSystemPrompts.fileAnalyst(toolContext)
                else -> AgentSystemPrompts.researchExpert(toolContext)
            }

            // Try to resume from the latest checkpoint
            val checkpoint = executionLogDao.getLatestCheckpoint(task.id)
            val resumeData = checkpoint?.checkpointData

            onProgress(0.05f, "Building research outline…")

            // Step 1: Generate research outline (or restore from checkpoint)
            val questions: List<String>
            if (resumeData != null) {
                Timber.i("ResearchAgent: resuming task ${task.id} from checkpoint")
                questions = resumeData.lines().filter { it.isNotBlank() }
                onProgress(0.15f, "Resumed from checkpoint — continuing research…")
            } else {
                val outlineResponse = api.chatCompletion(
                    ChatRequestDto(
                        model = Constants.DEFAULT_MODEL_ID,
                        messages = listOf(
                            ChatMessageDto(role = "system", content = systemPrompt),
                            ChatMessageDto(
                                role = "user",
                                content = "Create a research plan with exactly 4 key questions to thoroughly " +
                                    "answer this topic:\n\"${task.prompt}\"\n\n" +
                                    "Use any reference data in your system prompt to inform the questions. " +
                                    "Return ONLY the 4 questions, one per line, numbered 1-4."
                            )
                        ),
                        maxTokens = 512
                    )
                )

                val outlineText = outlineResponse.choices?.firstOrNull()?.message?.content
                    ?: return AgentExecutionResult.Failure("Failed to generate research outline")

                questions = outlineText.lines().filter { it.trim().isNotBlank() }.take(4)

                if (questions.isEmpty()) {
                    return AgentExecutionResult.Failure("Could not parse research questions from outline")
                }

                // Save checkpoint: outline generated
                executionLogDao.insert(
                    AgentExecutionLogEntity(
                        id = UUID.randomUUID().toString(),
                        taskId = task.id,
                        timestamp = System.currentTimeMillis(),
                        stepName = "outline",
                        stepType = LogStepType.CHECKPOINT,
                        inputSummary = task.prompt.take(300),
                        outputSummary = outlineText.take(300),
                        checkpointData = questions.joinToString("\n"),
                        durationMs = 0L,
                        toolUsed = null
                    )
                )
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
                            ChatMessageDto(role = "system", content = systemPrompt),
                            ChatMessageDto(
                                role = "user",
                                content = "Research context: \"${task.prompt}\"\n\n" +
                                    "Answer this question in depth (4-6 paragraphs): $question\n\n" +
                                    "Ground your answer in the reference data where possible. " +
                                    "Prefix claims from reference data with [DATA] " +
                                    "and prior knowledge with [PRIOR KNOWLEDGE]."
                            )
                        ),
                        maxTokens = Constants.AGENT_MAX_TOKENS
                    )
                )

                val answer = questionResponse.choices?.firstOrNull()?.message?.content ?: ""
                researchResults.append("## $question\n\n$answer\n\n")
            }

            onProgress(0.80f, "Synthesising final report…")

            // Final step: Synthesise into a comprehensive report
            val synthesisResponse = api.chatCompletion(
                ChatRequestDto(
                    model = Constants.DEFAULT_MODEL_ID,
                    messages = listOf(
                        ChatMessageDto(role = "system", content = systemPrompt),
                        ChatMessageDto(
                            role = "user",
                            content = "Research topic: \"${task.prompt}\"\n\n" +
                                "Synthesise the following research into a comprehensive, well-structured report:\n\n" +
                                researchResults.toString() +
                                "\n\nStructure: Executive Summary (3-4 sentences) → " +
                                "Key Findings (from the research above) → " +
                                "Conclusions & Recommendations → Further Research Areas."
                        )
                    ),
                    maxTokens = Constants.AGENT_MAX_TOKENS
                )
            )

            val synthesis = synthesisResponse.choices?.firstOrNull()?.message?.content
                ?: return AgentExecutionResult.Failure("Failed to synthesise research report")

            onProgress(1.0f, "Research complete")
            AgentExecutionResult.Success(synthesis)

        } catch (e: Exception) {
            Timber.e(e, "ResearchAgent failed for task ${task.id}")
            AgentExecutionResult.Failure(e.message ?: "Network error during research", isRetryable = true)
        }
    }
}
