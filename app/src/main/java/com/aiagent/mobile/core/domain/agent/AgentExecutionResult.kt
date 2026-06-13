package com.aiagent.mobile.core.domain.agent

sealed class AgentExecutionResult {
    data class Success(val output: String) : AgentExecutionResult()
    data class Failure(val error: String, val isRetryable: Boolean = true) : AgentExecutionResult()
    data object Cancelled : AgentExecutionResult()
}
