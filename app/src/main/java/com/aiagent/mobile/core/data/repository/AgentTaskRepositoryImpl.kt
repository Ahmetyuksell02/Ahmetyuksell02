package com.aiagent.mobile.core.data.repository

import com.aiagent.mobile.core.data.local.dao.AgentTaskDao
import com.aiagent.mobile.core.data.mapper.toDomain
import com.aiagent.mobile.core.data.mapper.toEntity
import com.aiagent.mobile.core.domain.model.AgentTask
import com.aiagent.mobile.core.domain.model.AgentTaskStatus
import com.aiagent.mobile.core.domain.repository.IAgentTaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgentTaskRepositoryImpl @Inject constructor(
    private val dao: AgentTaskDao
) : IAgentTaskRepository {

    override fun getAll(): Flow<List<AgentTask>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override fun getById(id: String): Flow<AgentTask?> =
        dao.getById(id).map { it?.toDomain() }

    override fun getByStatus(status: AgentTaskStatus): Flow<List<AgentTask>> =
        dao.getByStatus(status.name).map { list -> list.map { it.toDomain() } }

    override suspend fun insert(task: AgentTask) = dao.insert(task.toEntity())

    override suspend fun update(task: AgentTask) = dao.update(task.toEntity())

    override suspend fun delete(taskId: String) = dao.delete(taskId)

    override suspend fun updateStatus(
        taskId: String,
        status: AgentTaskStatus,
        error: String?
    ) = dao.updateStatus(taskId, status.name, error, System.currentTimeMillis())

    override suspend fun updateProgress(taskId: String, progress: Float) =
        dao.updateProgress(taskId, progress, System.currentTimeMillis())

    override suspend fun updateResult(
        taskId: String,
        result: String,
        status: AgentTaskStatus
    ) = dao.updateResult(taskId, result, status.name, System.currentTimeMillis())

    override suspend fun updateWorkerId(taskId: String, workerId: String) =
        dao.updateWorkerId(taskId, workerId)
}
