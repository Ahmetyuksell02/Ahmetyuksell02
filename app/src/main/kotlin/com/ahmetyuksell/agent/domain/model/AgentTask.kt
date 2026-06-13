package com.ahmetyuksell.agent.domain.model

data class AgentTask(
    val id: String,
    val agentId: String,
    val conversationId: String,
    val status: AgentTaskStatus,
    val inputText: String,
    val resultText: String? = null,
    val errorMessage: String? = null,
    val stepsJson: String? = null,
    val startedAt: Long? = null,
    val completedAt: Long? = null,
    val retryCount: Int = 0
)

enum class AgentTaskStatus {
    PENDING, RUNNING, COMPLETED, FAILED, CANCELLED
}
