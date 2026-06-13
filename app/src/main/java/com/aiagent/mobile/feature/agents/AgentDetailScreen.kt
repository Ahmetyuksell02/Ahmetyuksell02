package com.aiagent.mobile.feature.agents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.aiagent.mobile.core.domain.model.AgentTaskStatus
import com.aiagent.mobile.core.domain.repository.IAgentTaskRepository
import com.aiagent.mobile.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AgentDetailUiState(
    val task: AgentTaskUiModel? = null,
    val executionLogs: List<String> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class AgentDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val agentTaskRepository: IAgentTaskRepository
) : ViewModel() {

    private val taskId: String = savedStateHandle.get<String>(Screen.AgentDetail.ARG_TASK_ID) ?: ""

    private val _uiState = MutableStateFlow(AgentDetailUiState(isLoading = true))
    val uiState: StateFlow<AgentDetailUiState> = _uiState.asStateFlow()

    init {
        observeTask()
    }

    private fun observeTask() {
        viewModelScope.launch {
            agentTaskRepository.getById(taskId)
                .collect { task ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            task = task?.let { t ->
                                AgentTaskUiModel(
                                    id = t.id,
                                    title = t.title,
                                    description = t.description,
                                    type = t.taskType,
                                    status = t.status,
                                    progress = t.progress,
                                    resultSummary = t.result?.take(300)
                                )
                            }
                        )
                    }
                }
        }
    }

    fun pauseTask() {
        viewModelScope.launch {
            agentTaskRepository.updateStatus(taskId, AgentTaskStatus.PAUSED)
        }
    }

    fun resumeTask() {
        viewModelScope.launch {
            agentTaskRepository.updateStatus(taskId, AgentTaskStatus.PENDING)
        }
    }

    fun cancelTask() {
        viewModelScope.launch {
            agentTaskRepository.updateStatus(taskId, AgentTaskStatus.FAILED, "Cancelled by user")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: AgentDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.task?.title ?: "Agent Detail") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            uiState.task?.let { task ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Status card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Status: ${task.status.name}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                StatusIcon(status = task.status)
                            }

                            if (task.status == AgentTaskStatus.RUNNING) {
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { task.progress },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    text = "${(task.progress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Description card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Task Description",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = task.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Result summary
                    task.resultSummary?.let { summary ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Result",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = summary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        when (task.status) {
                            AgentTaskStatus.RUNNING -> {
                                OutlinedButton(
                                    onClick = viewModel::pauseTask,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Filled.Pause, contentDescription = null)
                                    Text("Pause")
                                }
                            }
                            AgentTaskStatus.PAUSED -> {
                                Button(
                                    onClick = viewModel::resumeTask,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                                    Text("Resume")
                                }
                            }
                            else -> {}
                        }

                        if (task.status != AgentTaskStatus.COMPLETED) {
                            Button(
                                onClick = viewModel::cancelTask,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.Stop, contentDescription = null)
                                Text("Cancel")
                            }
                        }
                    }
                }
            }
        }
    }
}
