package com.ahmetyuksell.agent.agent

import com.ahmetyuksell.agent.data.remote.api.OpenRouterApi
import com.ahmetyuksell.agent.data.remote.dto.ChatCompletionRequestDto
import com.ahmetyuksell.agent.data.remote.dto.MessageDto
import com.ahmetyuksell.agent.domain.agent.AgentStep
import com.ahmetyuksell.agent.domain.agent.ToolRegistry
import kotlinx.serialization.json.JsonPrimitive
import timber.log.Timber
import javax.inject.Inject

sealed class PlannerDecision {
    data class UseToolAction(
        val toolName: String,
        val argsJson: String,
        val thought: String
    ) : PlannerDecision()

    data class FinalAnswer(
        val answer: String,
        val thought: String
    ) : PlannerDecision()

    data class Error(val message: String) : PlannerDecision()
}

class AgentPlanner @Inject constructor(
    private val api: OpenRouterApi,
    private val toolRegistry: ToolRegistry
) {
    suspend fun decide(
        taskDescription: String,
        agentSystemPrompt: String,
        modelId: String,
        history: List<AgentStep>
    ): PlannerDecision {
        val toolSchemas = toolRegistry.getToolSchemas()
        val systemPrompt = buildSystemPrompt(agentSystemPrompt, toolSchemas)
        val conversationMessages = buildConversationMessages(taskDescription, history)

        val allMessages = mutableListOf(
            MessageDto(role = "system", content = JsonPrimitive(systemPrompt))
        ) + conversationMessages

        return try {
            val response = api.chatCompletion(
                ChatCompletionRequestDto(
                    model = modelId,
                    messages = allMessages,
                    temperature = 0.2
                )
            )

            val content = response.choices.firstOrNull()?.message?.content ?: ""
            parseResponse(content)
        } catch (e: Exception) {
            Timber.e(e, "AgentPlanner error")
            PlannerDecision.Error(e.message ?: "Planning failed")
        }
    }

    private fun buildSystemPrompt(agentSystemPrompt: String, toolSchemas: List<String>): String {
        val toolList = toolSchemas.joinToString("\n")
        return """
$agentSystemPrompt

You are an autonomous AI agent. You must respond in this exact format:

Thought: <your reasoning about what to do>
Action: <tool_name> OR "final_answer"
Action Input: <JSON args for the tool, OR your final response if action is final_answer>

Available tools:
$toolList

Rules:
- Always start with "Thought:"
- Use exactly one of the available tools, OR use "final_answer" to conclude
- Action Input must be valid JSON for tools
- Be concise in thoughts
- When you have enough information to answer, use final_answer
        """.trimIndent()
    }

    private fun buildConversationMessages(
        task: String,
        history: List<AgentStep>
    ): List<MessageDto> {
        val messages = mutableListOf<MessageDto>()

        messages.add(
            MessageDto(role = "user", content = JsonPrimitive("Task: $task"))
        )

        for (step in history) {
            val assistantContent = buildString {
                append("Thought: ${step.thought}\n")
                step.action?.let { append("Action: $it\n") }
                step.actionInput?.let { append("Action Input: $it\n") }
            }
            messages.add(MessageDto(role = "assistant", content = JsonPrimitive(assistantContent)))

            step.observation?.let { obs ->
                messages.add(MessageDto(role = "user", content = JsonPrimitive("Observation: $obs")))
            }
        }

        return messages
    }

    private fun parseResponse(content: String): PlannerDecision {
        val thought = extractField(content, "Thought") ?: ""
        val action = extractField(content, "Action") ?: ""
        val actionInput = extractField(content, "Action Input") ?: "{}"

        return when {
            action.isBlank() -> PlannerDecision.Error("Could not parse action from: $content")
            action.trim().lowercase() == "final_answer" ->
                PlannerDecision.FinalAnswer(answer = actionInput.trim(), thought = thought)
            else ->
                PlannerDecision.UseToolAction(
                    toolName = action.trim(),
                    argsJson = actionInput.trim(),
                    thought = thought
                )
        }
    }

    private fun extractField(text: String, field: String): String? {
        val regex = Regex("(?i)$field:\\s*(.+?)(?=\\n(?:Thought|Action|Action Input):|$)", RegexOption.DOT_MATCHES_ALL)
        return regex.find(text)?.groupValues?.getOrNull(1)?.trim()
    }
}
