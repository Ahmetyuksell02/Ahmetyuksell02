package com.ahmetyuksell.agent.agent

import com.ahmetyuksell.agent.domain.agent.AgentStep
import com.ahmetyuksell.agent.util.TokenEstimator
import javax.inject.Inject

class AgentMemoryManager @Inject constructor() {

    fun shouldCompress(steps: List<AgentStep>, contextLimit: Int): Boolean {
        val totalTokens = steps.sumOf { estimateStepTokens(it) }
        return totalTokens > contextLimit * 0.7
    }

    fun compressSteps(steps: List<AgentStep>): List<AgentStep> {
        if (steps.size <= 2) return steps

        val recentSteps = steps.takeLast(4)
        val olderSteps = steps.dropLast(4)

        val summarized = AgentStep(
            stepNumber = 0,
            thought = "Summary of ${olderSteps.size} previous steps",
            observation = buildSummary(olderSteps)
        )

        return listOf(summarized) + recentSteps
    }

    private fun buildSummary(steps: List<AgentStep>): String {
        return steps.joinToString("\n") { step ->
            buildString {
                append("Step ${step.stepNumber}: ${step.thought}")
                step.action?.let { append(" → used $it") }
                step.observation?.let { append(" → got: ${it.take(200)}") }
            }
        }
    }

    private fun estimateStepTokens(step: AgentStep): Int {
        val text = listOfNotNull(step.thought, step.action, step.actionInput, step.observation)
            .joinToString(" ")
        return TokenEstimator.estimate(text)
    }
}
