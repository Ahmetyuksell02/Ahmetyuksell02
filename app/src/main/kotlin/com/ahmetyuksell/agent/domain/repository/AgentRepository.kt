package com.ahmetyuksell.agent.domain.repository

import com.ahmetyuksell.agent.domain.model.Agent
import com.ahmetyuksell.agent.domain.model.AgentTask
import com.ahmetyuksell.agent.domain.model.AgentTaskStatus
import kotlinx.coroutines.flow.Flow

interface AgentRepository {
    fun getAllAgents(): Flow<List<Agent>>
    suspend fun getAgent(id: String): Agent?
    suspend fun insertAgent(agent: Agent): String
    suspend fun updateAgent(agent: Agent)
    suspend fun deleteAgent(id: String)
}

interface AgentTaskRepository {
    fun getTasksForAgent(agentId: String): Flow<List<AgentTask>>
    fun getRunningTasks(): Flow<List<AgentTask>>
    fun getAllTasks(): Flow<List<AgentTask>>
    suspend fun getTask(id: String): AgentTask?
    suspend fun insertTask(task: AgentTask): String
    suspend fun updateTask(task: AgentTask)
    suspend fun updateTaskStatus(id: String, status: AgentTaskStatus)
    suspend fun updateTaskResult(id: String, result: String, steps: String)
    suspend fun updateTaskError(id: String, error: String)
    suspend fun cancelTask(id: String)
}
