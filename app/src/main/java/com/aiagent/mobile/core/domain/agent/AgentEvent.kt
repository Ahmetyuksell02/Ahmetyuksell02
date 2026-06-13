package com.aiagent.mobile.core.domain.agent

sealed class AgentEvent {
    data class Started(val taskId: String, val title: String) : AgentEvent()
    data class Progress(val taskId: String, val progress: Float, val message: String) : AgentEvent()
    data class Completed(val taskId: String, val result: String) : AgentEvent()
    data class Failed(val taskId: String, val error: String) : AgentEvent()
    data class Retrying(val taskId: String, val attempt: Int, val maxAttempts: Int) : AgentEvent()
    data class Paused(val taskId: String) : AgentEvent()
}
