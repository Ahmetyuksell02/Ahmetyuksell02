package com.aiagent.mobile.feature.agents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiagent.mobile.core.domain.model.AgentTask
import com.aiagent.mobile.core.domain.model.AgentTaskStatus
import com.aiagent.mobile.core.domain.model.AgentTaskType
import com.aiagent.mobile.core.domain.repository.IAgentTaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class AgentTaskUiModel(
    val id: String,
    val title: String,
    val description: String,
    val type: AgentTaskType,
    val status: AgentTaskStatus,
    val progress: Float = 0f,
    val nextRunFormatted: String? = null,
    val lastRunFormatted: String? = null,
    val resultSummary: String? = null
)

data class AgentsUiState(
    val tasks: List<AgentTaskUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AgentsViewModel @Inject constructor(
    private val agentTaskRepository: IAgentTaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AgentsUiState(isLoading = true))
    val uiState: StateFlow<AgentsUiState> = _uiState.asStateFlow()

    init {
        observeTasks()
    }

    private fun observeTasks() {
        viewModelScope.launch {
            agentTaskRepository.getAll()
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { tasks ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            tasks = tasks.map { t -> t.toUiModel() }
                        )
                    }
                }
        }
    }

    fun pauseTask(taskId: String) {
        viewModelScope.launch {
            agentTaskRepository.updateStatus(taskId, AgentTaskStatus.PAUSED)
            // WorkManager cancellation wired in Phase 4
        }
    }

    fun resumeTask(taskId: String) {
        viewModelScope.launch {
            agentTaskRepository.updateStatus(taskId, AgentTaskStatus.PENDING)
            // WorkManager re-enqueue wired in Phase 4
        }
    }

    fun cancelTask(taskId: String) {
        viewModelScope.launch {
            agentTaskRepository.updateStatus(taskId, AgentTaskStatus.FAILED, "Cancelled by user")
            // WorkManager cancellation wired in Phase 4
        }
    }

    fun retryTask(taskId: String) {
        viewModelScope.launch {
            agentTaskRepository.updateStatus(taskId, AgentTaskStatus.PENDING)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun AgentTask.toUiModel(): AgentTaskUiModel {
        val fmt = SimpleDateFormat("MMM dd HH:mm", Locale.getDefault())
        return AgentTaskUiModel(
            id = id,
            title = title,
            description = description,
            type = taskType,
            status = status,
            progress = progress,
            nextRunFormatted = nextRunAt?.let { fmt.format(Date(it)) },
            lastRunFormatted = lastRunAt?.let { fmt.format(Date(it)) },
            resultSummary = result?.take(120)
        )
    }
}
