package com.aiagent.mobile.feature.agents

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.aiagent.mobile.core.data.scheduler.AgentScheduler
import com.aiagent.mobile.core.domain.model.AgentTask
import com.aiagent.mobile.core.domain.model.AgentTaskStatus
import com.aiagent.mobile.core.domain.model.AgentTaskType
import com.aiagent.mobile.core.domain.model.TriggerType
import com.aiagent.mobile.core.domain.repository.IAgentTaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class CreateAgentUiState(
    val title: String = "",
    val prompt: String = "",
    val taskType: AgentTaskType = AgentTaskType.RESEARCH,
    val isPeriodic: Boolean = false,
    val intervalHours: String = "24",
    val scheduledTime: String = "",
    val isSaving: Boolean = false,
    val titleError: String? = null,
    val promptError: String? = null
)

@HiltViewModel
class CreateAgentViewModel @Inject constructor(
    private val agentTaskRepository: IAgentTaskRepository,
    private val agentScheduler: AgentScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateAgentUiState())
    val uiState: StateFlow<CreateAgentUiState> = _uiState.asStateFlow()

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title, titleError = null) }
    }

    fun updatePrompt(prompt: String) {
        _uiState.update { it.copy(prompt = prompt, promptError = null) }
    }

    fun updateTaskType(type: AgentTaskType) {
        _uiState.update { it.copy(taskType = type) }
    }

    fun updateIsPeriodic(isPeriodic: Boolean) {
        _uiState.update { it.copy(isPeriodic = isPeriodic) }
    }

    fun updateIntervalHours(hours: String) {
        _uiState.update { it.copy(intervalHours = hours) }
    }

    fun createAgent(onSuccess: () -> Unit) {
        val state = _uiState.value
        var isValid = true

        if (state.title.isBlank()) {
            _uiState.update { it.copy(titleError = "Title is required") }
            isValid = false
        }
        if (state.prompt.isBlank()) {
            _uiState.update { it.copy(promptError = "Task prompt is required") }
            isValid = false
        }
        if (!isValid) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val now = System.currentTimeMillis()
            val task = AgentTask(
                id = UUID.randomUUID().toString(),
                title = state.title.trim(),
                description = state.prompt.take(200).trim(),
                prompt = state.prompt.trim(),
                taskType = state.taskType,
                status = AgentTaskStatus.PENDING,
                isPeriodic = state.isPeriodic,
                intervalMinutes = (state.intervalHours.toLongOrNull() ?: 24L) * 60L,
                triggerType = TriggerType.MANUAL,
                createdAt = now,
                updatedAt = now
            )
            agentTaskRepository.insert(task)
            agentScheduler.schedule(task)
            _uiState.update { it.copy(isSaving = false) }
            onSuccess()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAgentScreen(
    onNavigateBack: () -> Unit,
    viewModel: CreateAgentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var typeDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Create Agent",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Configure a background agent to run autonomous tasks",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::updateTitle,
                label = { Text("Agent Name") },
                placeholder = { Text("e.g. Daily AI News Summary") },
                isError = uiState.titleError != null,
                supportingText = uiState.titleError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Task type selector
            ExposedDropdownMenuBox(
                expanded = typeDropdownExpanded,
                onExpandedChange = { typeDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = uiState.taskType.name.replace("_", " "),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Task Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = typeDropdownExpanded,
                    onDismissRequest = { typeDropdownExpanded = false }
                ) {
                    AgentTaskType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.name.replace("_", " ")) },
                            onClick = {
                                viewModel.updateTaskType(type)
                                typeDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = uiState.prompt,
                onValueChange = viewModel::updatePrompt,
                label = { Text("Task Prompt") },
                placeholder = { Text("Describe what the agent should do…") },
                isError = uiState.promptError != null,
                supportingText = uiState.promptError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                maxLines = 8
            )

            // Periodic scheduling toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Run Periodically",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Repeat this task on a schedule",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = uiState.isPeriodic,
                    onCheckedChange = viewModel::updateIsPeriodic
                )
            }

            if (uiState.isPeriodic) {
                OutlinedTextField(
                    value = uiState.intervalHours,
                    onValueChange = viewModel::updateIntervalHours,
                    label = { Text("Interval (hours)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.createAgent(onNavigateBack) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving
            ) {
                Text(if (uiState.isSaving) "Creating…" else "Create Agent")
            }
        }
    }
}
