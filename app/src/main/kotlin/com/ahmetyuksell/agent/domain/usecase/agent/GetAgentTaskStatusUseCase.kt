package com.ahmetyuksell.agent.domain.usecase.agent

import com.ahmetyuksell.agent.domain.model.AgentTask
import com.ahmetyuksell.agent.domain.repository.AgentTaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAgentTaskStatusUseCase @Inject constructor(
    private val agentTaskRepository: AgentTaskRepository
) {
    fun getRunning(): Flow<List<AgentTask>> = agentTaskRepository.getRunningTasks()
    fun getAll(): Flow<List<AgentTask>> = agentTaskRepository.getAllTasks()
    fun getForAgent(agentId: String): Flow<List<AgentTask>> =
        agentTaskRepository.getTasksForAgent(agentId)
}
