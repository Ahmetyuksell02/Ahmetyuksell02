package com.ahmetyuksell.agent.presentation.screens.agents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahmetyuksell.agent.domain.model.Agent
import com.ahmetyuksell.agent.domain.repository.AgentRepository
import com.ahmetyuksell.agent.domain.usecase.agent.CancelAgentTaskUseCase
import com.ahmetyuksell.agent.domain.usecase.agent.GetAgentTaskStatusUseCase
import com.ahmetyuksell.agent.domain.usecase.agent.StartAgentTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AgentViewModel @Inject constructor(
    private val agentRepository: AgentRepository,
    private val startAgentTaskUseCase: StartAgentTaskUseCase,
    private val cancelAgentTaskUseCase: CancelAgentTaskUseCase,
    private val getAgentTaskStatusUseCase: GetAgentTaskStatusUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AgentUiState())
    val uiState: StateFlow<AgentUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            agentRepository.getAllAgents().collect { agents ->
                _uiState.update { it.copy(agents = agents) }
            }
        }
        viewModelScope.launch {
            getAgentTaskStatusUseCase.getRunning().collect { tasks ->
                _uiState.update { it.copy(runningTasks = tasks) }
            }
        }
        viewModelScope.launch {
            getAgentTaskStatusUseCase.getAll().collect { tasks ->
                _uiState.update { it.copy(allTasks = tasks.take(20)) }
            }
        }
    }

    fun startTask(agentId: String, conversationId: String, input: String) {
        viewModelScope.launch {
            try {
                startAgentTaskUseCase(agentId, conversationId, input)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun cancelTask(taskId: String) {
        viewModelScope.launch {
            cancelAgentTaskUseCase(taskId)
        }
    }

    fun createAgent(
        name: String,
        description: String,
        systemPrompt: String,
        modelId: String
    ) {
        viewModelScope.launch {
            val agent = Agent(
                id = UUID.randomUUID().toString(),
                name = name,
                description = description,
                systemPrompt = systemPrompt,
                modelId = modelId,
                toolsJson = """["web_search","calculator","get_datetime","get_weather","get_exchange_rate","get_news"]""",
                createdAt = System.currentTimeMillis()
            )
            agentRepository.insertAgent(agent)
        }
    }

    fun deleteAgent(id: String) {
        viewModelScope.launch {
            agentRepository.deleteAgent(id)
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
