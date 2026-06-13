package com.ahmetyuksell.agent.presentation.screens.agents

import com.ahmetyuksell.agent.domain.model.Agent
import com.ahmetyuksell.agent.domain.model.AgentTask

data class AgentUiState(
    val agents: List<Agent> = emptyList(),
    val runningTasks: List<AgentTask> = emptyList(),
    val allTasks: List<AgentTask> = emptyList(),
    val taskProgress: Map<String, String> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
)
