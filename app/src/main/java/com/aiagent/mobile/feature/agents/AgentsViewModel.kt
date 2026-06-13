package com.aiagent.mobile.feature.agents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiagent.mobile.core.data.scheduler.AgentScheduler
import com.aiagent.mobile.core.domain.agent.AgentEvent
import com.aiagent.mobile.core.domain.agent.AgentEventBus
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
    val resultSummary: String? = null,
    val retryCount: Int = 0,
    val maxRetries: Int = 3
)

data class AgentsUiState(
    val tasks: List<AgentTaskUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastEvent: String? = null
)

@HiltViewModel
class AgentsViewModel @Inject constructor(
    private val agentTaskRepository: IAgentTaskRepository,
    private val agentScheduler: AgentScheduler,
    private val agentEventBus: AgentEventBus
) : ViewModel() {

    private val _uiState = MutableStateFlow(AgentsUiState(isLoading = true))
    val uiState: StateFlow<AgentsUiState> = _uiState.asStateFlow()

    init {
        observeTasks()
        observeAgentEvents()
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

    private fun observeAgentEvents() {
        viewModelScope.launch {
            agentEventBus.events.collect { event ->
                val message = when (event) {
                    is AgentEvent.Started -> "Agent \"${event.title}\" started"
                    is AgentEvent.Progress -> null // Room update handles the UI; no toast needed
                    is AgentEvent.Completed -> "Agent completed successfully"
                    is AgentEvent.Failed -> "Agent failed: ${event.error.take(60)}"
                    is AgentEvent.Retrying -> "Agent retrying (${event.attempt}/${event.maxAttempts})"
                    is AgentEvent.Paused -> "Agent paused"
                }
                if (message != null) {
                    _uiState.update { it.copy(lastEvent = message) }
                }
            }
        }
    }

    fun pauseTask(taskId: String) {
        viewModelScope.launch {
            agentScheduler.cancel(taskId)
            agentTaskRepository.updateStatus(taskId, AgentTaskStatus.PAUSED)
        }
    }

    fun resumeTask(taskId: String) {
        viewModelScope.launch {
            val task = agentTaskRepository.getByIdOnce(taskId) ?: return@launch
            agentTaskRepository.updateStatus(taskId, AgentTaskStatus.PENDING)
            agentScheduler.schedule(task)
        }
    }

    fun cancelTask(taskId: String) {
        viewModelScope.launch {
            agentScheduler.cancel(taskId)
            agentTaskRepository.updateStatus(taskId, AgentTaskStatus.FAILED, "Cancelled by user")
        }
    }

    fun retryTask(taskId: String) {
        viewModelScope.launch {
            val task = agentTaskRepository.getByIdOnce(taskId) ?: return@launch
            agentTaskRepository.updateRetryCount(taskId, 0)
            agentTaskRepository.updateStatus(taskId, AgentTaskStatus.PENDING)
            agentScheduler.schedule(task)
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            agentScheduler.cancel(taskId)
            agentTaskRepository.delete(taskId)
        }
    }

    fun clearLastEvent() {
        _uiState.update { it.copy(lastEvent = null) }
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
            resultSummary = result?.take(120),
            retryCount = retryCount,
            maxRetries = maxRetries
        )
    }
}
