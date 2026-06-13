package com.ahmetyuksell.agent.agent

import com.ahmetyuksell.agent.domain.agent.AgentOrchestrator
import com.ahmetyuksell.agent.domain.agent.AgentResult
import com.ahmetyuksell.agent.domain.agent.AgentStep
import com.ahmetyuksell.agent.domain.agent.ToolRegistry
import com.ahmetyuksell.agent.domain.model.AgentTask
import com.ahmetyuksell.agent.domain.repository.AgentRepository
import com.ahmetyuksell.agent.util.TokenEstimator
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import timber.log.Timber
import java.util.Collections
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgentOrchestratorImpl @Inject constructor(
    private val planner: AgentPlanner,
    private val toolRegistry: ToolRegistry,
    private val memoryManager: AgentMemoryManager,
    private val agentRepository: AgentRepository
) : AgentOrchestrator {

    private val cancelledTasks: MutableSet<String> =
        Collections.synchronizedSet(mutableSetOf())

    override suspend fun execute(
        task: AgentTask,
        onProgress: (step: Int, thought: String) -> Unit
    ): AgentResult {
        val agent = agentRepository.getAgent(task.agentId)
            ?: return AgentResult.Failed("Agent ${task.agentId} not found", emptyList())

        val steps = mutableListOf<AgentStep>()
        // Rolling window of recent tool calls for stuck-loop detection
        val recentToolCalls = ArrayDeque<String>(LOOP_WINDOW)
        var accumulatedTokens = TokenEstimator.estimate(task.inputText + agent.systemPrompt)

        Timber.i("Agent[${task.id}] starting: agent=${agent.name} model=${agent.modelId}")

        for (stepNumber in 1..MAX_STEPS) {
            currentCoroutineContext().ensureActive()

            if (task.id in cancelledTasks) {
                cancelledTasks.remove(task.id)
                Timber.i("Agent[${task.id}] cancelled at step $stepNumber")
                return AgentResult.Cancelled
            }

            if (accumulatedTokens > TOKEN_BUDGET) {
                Timber.w("Agent[${task.id}] token budget exceeded: ~$accumulatedTokens tokens at step $stepNumber")
                return AgentResult.Failed(
                    "Token budget exceeded (~$accumulatedTokens tokens). Provide a more specific task.",
                    steps
                )
            }

            val workingSteps = if (memoryManager.shouldCompress(steps, CONTEXT_LIMIT)) {
                Timber.d("Agent[${task.id}] compressing context at step $stepNumber")
                memoryManager.compressSteps(steps)
            } else {
                steps.toList()
            }

            val decision = planner.decide(
                taskDescription = task.inputText,
                agentSystemPrompt = agent.systemPrompt,
                modelId = agent.modelId,
                history = workingSteps
            )

            when (decision) {
                is PlannerDecision.FinalAnswer -> {
                    steps.add(
                        AgentStep(
                            stepNumber = stepNumber,
                            thought = decision.thought,
                            action = "final_answer",
                            actionInput = decision.answer
                        )
                    )
                    onProgress(stepNumber, decision.thought)
                    Timber.i("Agent[${task.id}] completed in $stepNumber steps (~$accumulatedTokens tokens)")
                    return AgentResult.Success(decision.answer, steps)
                }

                is PlannerDecision.UseToolAction -> {
                    // Detect infinite loops: same tool+args repeated within the recent window
                    val callKey = "${decision.toolName}::${decision.argsJson}"
                    if (callKey in recentToolCalls) {
                        Timber.e("Agent[${task.id}] stuck loop at step $stepNumber: ${decision.toolName} repeated with same args")
                        return AgentResult.Failed(
                            "Agent stuck in a loop: '${decision.toolName}' called with identical arguments. " +
                                    "Try rephrasing the task.",
                            steps
                        )
                    }
                    if (recentToolCalls.size >= LOOP_WINDOW) recentToolCalls.removeFirst()
                    recentToolCalls.addLast(callKey)

                    onProgress(stepNumber, decision.thought)
                    Timber.d("Agent[${task.id}] step $stepNumber: tool=${decision.toolName}")

                    val toolResult = toolRegistry.execute(decision.toolName, decision.argsJson)
                    val observation = if (toolResult.success) toolResult.result
                    else "Error: ${toolResult.errorMessage}"

                    accumulatedTokens += TokenEstimator.estimate(
                        decision.thought + decision.argsJson + observation
                    )

                    steps.add(
                        AgentStep(
                            stepNumber = stepNumber,
                            thought = decision.thought,
                            action = decision.toolName,
                            actionInput = decision.argsJson,
                            observation = observation
                        )
                    )

                    if (!toolResult.success) {
                        Timber.w("Agent[${task.id}] step $stepNumber tool error: ${toolResult.errorMessage}")
                    } else {
                        Timber.d("Agent[${task.id}] step $stepNumber: ${decision.toolName} → ${toolResult.result.take(80)}")
                    }
                }

                is PlannerDecision.Error -> {
                    Timber.e("Agent[${task.id}] planning error at step $stepNumber: ${decision.message}")
                    return AgentResult.Failed(decision.message, steps)
                }
            }
        }

        Timber.e("Agent[${task.id}] hit MAX_STEPS ($MAX_STEPS) without final answer")
        return AgentResult.Failed("Max steps ($MAX_STEPS) reached without final answer", steps)
    }

    override suspend fun cancel(taskId: String) {
        cancelledTasks.add(taskId)
    }

    companion object {
        private const val MAX_STEPS = 15
        private const val CONTEXT_LIMIT = 6000
        // ~5000 words across all steps before stopping to prevent cost runaway
        private const val TOKEN_BUDGET = 20_000
        // Detect if the same tool+args pair repeats within this many recent calls
        private const val LOOP_WINDOW = 3
    }
}
