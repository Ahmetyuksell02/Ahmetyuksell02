package com.ahmetyuksell.agent.agent

import com.ahmetyuksell.agent.domain.agent.AgentOrchestrator
import com.ahmetyuksell.agent.domain.agent.AgentResult
import com.ahmetyuksell.agent.domain.agent.AgentStep
import com.ahmetyuksell.agent.domain.agent.ToolRegistry
import com.ahmetyuksell.agent.domain.model.AgentTask
import com.ahmetyuksell.agent.domain.repository.AgentRepository
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

        for (stepNumber in 1..MAX_STEPS) {
            currentCoroutineContext().ensureActive()

            if (task.id in cancelledTasks) {
                cancelledTasks.remove(task.id)
                return AgentResult.Cancelled
            }

            val workingSteps = if (memoryManager.shouldCompress(steps, CONTEXT_LIMIT)) {
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
                    return AgentResult.Success(decision.answer, steps)
                }

                is PlannerDecision.UseToolAction -> {
                    onProgress(stepNumber, decision.thought)
                    val toolResult = toolRegistry.execute(decision.toolName, decision.argsJson)
                    steps.add(
                        AgentStep(
                            stepNumber = stepNumber,
                            thought = decision.thought,
                            action = decision.toolName,
                            actionInput = decision.argsJson,
                            observation = if (toolResult.success) toolResult.result
                            else "Error: ${toolResult.errorMessage}"
                        )
                    )
                    Timber.d("Step $stepNumber: ${decision.toolName} → ${toolResult.result.take(80)}")
                }

                is PlannerDecision.Error -> {
                    Timber.e("Planning error at step $stepNumber: ${decision.message}")
                    return AgentResult.Failed(decision.message, steps)
                }
            }
        }

        return AgentResult.Failed("Max steps ($MAX_STEPS) reached without final answer", steps)
    }

    override suspend fun cancel(taskId: String) {
        cancelledTasks.add(taskId)
    }

    companion object {
        private const val MAX_STEPS = 15
        private const val CONTEXT_LIMIT = 6000
    }
}
