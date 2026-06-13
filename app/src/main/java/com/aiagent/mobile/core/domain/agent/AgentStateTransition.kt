package com.aiagent.mobile.core.domain.agent

import com.aiagent.mobile.core.domain.model.AgentTaskStatus

object AgentStateTransition {

    private val validTransitions: Map<AgentTaskStatus, Set<AgentTaskStatus>> = mapOf(
        AgentTaskStatus.PENDING to setOf(
            AgentTaskStatus.SCHEDULED, AgentTaskStatus.RUNNING, AgentTaskStatus.FAILED
        ),
        AgentTaskStatus.SCHEDULED to setOf(
            AgentTaskStatus.RUNNING, AgentTaskStatus.PAUSED, AgentTaskStatus.FAILED
        ),
        AgentTaskStatus.RUNNING to setOf(
            AgentTaskStatus.COMPLETED, AgentTaskStatus.FAILED,
            AgentTaskStatus.PAUSED, AgentTaskStatus.RETRYING
        ),
        AgentTaskStatus.PAUSED to setOf(
            AgentTaskStatus.PENDING, AgentTaskStatus.RUNNING, AgentTaskStatus.FAILED
        ),
        AgentTaskStatus.COMPLETED to setOf(
            AgentTaskStatus.PENDING   // periodic re-runs
        ),
        AgentTaskStatus.FAILED to setOf(
            AgentTaskStatus.PENDING, AgentTaskStatus.RETRYING
        ),
        AgentTaskStatus.RETRYING to setOf(
            AgentTaskStatus.RUNNING, AgentTaskStatus.FAILED
        )
    )

    fun isValid(from: AgentTaskStatus, to: AgentTaskStatus): Boolean =
        validTransitions[from]?.contains(to) == true

    fun validate(from: AgentTaskStatus, to: AgentTaskStatus) {
        require(isValid(from, to)) {
            "Invalid agent state transition: $from → $to"
        }
    }
}
