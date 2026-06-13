package com.ahmetyuksell.agent.data.repository

import com.ahmetyuksell.agent.data.local.dao.AgentDao
import com.ahmetyuksell.agent.data.local.dao.AgentTaskDao
import com.ahmetyuksell.agent.data.mapper.toDomain
import com.ahmetyuksell.agent.data.mapper.toEntity
import com.ahmetyuksell.agent.domain.model.Agent
import com.ahmetyuksell.agent.domain.model.AgentTask
import com.ahmetyuksell.agent.domain.model.AgentTaskStatus
import com.ahmetyuksell.agent.domain.repository.AgentRepository
import com.ahmetyuksell.agent.domain.repository.AgentTaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgentRepositoryImpl @Inject constructor(
    private val agentDao: AgentDao
) : AgentRepository {

    override fun getAllAgents(): Flow<List<Agent>> =
        agentDao.getAllAgents().map { list -> list.map { it.toDomain() } }

    override suspend fun getAgent(id: String): Agent? =
        agentDao.getAgent(id)?.toDomain()

    override suspend fun insertAgent(agent: Agent): String {
        agentDao.insertAgent(agent.toEntity())
        return agent.id
    }

    override suspend fun updateAgent(agent: Agent) {
        agentDao.updateAgent(agent.toEntity())
    }

    override suspend fun deleteAgent(id: String) {
        agentDao.deleteAgent(id)
    }
}

@Singleton
class AgentTaskRepositoryImpl @Inject constructor(
    private val dao: AgentTaskDao
) : AgentTaskRepository {

    override fun getTasksForAgent(agentId: String): Flow<List<AgentTask>> =
        dao.getTasksForAgent(agentId).map { list -> list.map { it.toDomain() } }

    override fun getRunningTasks(): Flow<List<AgentTask>> =
        dao.getRunningTasks().map { list -> list.map { it.toDomain() } }

    override fun getAllTasks(): Flow<List<AgentTask>> =
        dao.getAllTasks().map { list -> list.map { it.toDomain() } }

    override suspend fun getTask(id: String): AgentTask? =
        dao.getTask(id)?.toDomain()

    override suspend fun insertTask(task: AgentTask): String {
        dao.insertTask(task.toEntity())
        return task.id
    }

    override suspend fun updateTask(task: AgentTask) {
        dao.updateTask(task.toEntity())
    }

    override suspend fun updateTaskStatus(id: String, status: AgentTaskStatus) {
        dao.updateStatus(id, status.name)
    }

    override suspend fun updateTaskResult(id: String, result: String, steps: String) {
        dao.updateResult(id, result, steps, System.currentTimeMillis())
    }

    override suspend fun updateTaskError(id: String, error: String) {
        dao.updateError(id, error, System.currentTimeMillis())
    }

    override suspend fun cancelTask(id: String) {
        dao.cancelTask(id, System.currentTimeMillis())
    }

    override suspend fun resetOrphanedRunningTasks(): Int {
        return dao.resetOrphanedTasks(System.currentTimeMillis())
    }
}
