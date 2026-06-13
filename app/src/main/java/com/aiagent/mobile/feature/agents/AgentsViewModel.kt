package com.aiagent.mobile.feature.agents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AgentTaskStatus { PENDING, RUNNING, COMPLETED, FAILED, PAUSED }

enum class AgentTaskType {
    NEWS_SUMMARY, PRICE_MONITOR, JOB_MONITOR,
    FILE_ANALYSIS, RESEARCH, WEB_INVESTIGATION, CUSTOM
}

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
class AgentsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(AgentsUiState())
    val uiState: StateFlow<AgentsUiState> = _uiState.asStateFlow()

    init {
        loadTasks()
    }

    private fun loadTasks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // WorkManager + Room query wired in Phase 4
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun pauseTask(taskId: String) {
        viewModelScope.launch {
            // Phase 4
        }
    }

    fun resumeTask(taskId: String) {
        viewModelScope.launch {
            // Phase 4
        }
    }

    fun cancelTask(taskId: String) {
        viewModelScope.launch {
            // Phase 4
        }
    }

    fun retryTask(taskId: String) {
        viewModelScope.launch {
            // Phase 4
        }
    }
}
