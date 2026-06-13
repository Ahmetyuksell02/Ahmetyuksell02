package com.aiagent.mobile.core.domain.repository

import com.aiagent.mobile.core.domain.model.AgentTask
import com.aiagent.mobile.core.domain.model.AgentTaskStatus
import kotlinx.coroutines.flow.Flow

interface IAgentTaskRepository {
    fun getAll(): Flow<List<AgentTask>>
    fun getById(id: String): Flow<AgentTask?>
    fun getByStatus(status: AgentTaskStatus): Flow<List<AgentTask>>
    fun getByStatuses(statuses: List<AgentTaskStatus>): Flow<List<AgentTask>>
    suspend fun getByIdOnce(id: String): AgentTask?
    suspend fun getByStatusesOnce(statuses: List<AgentTaskStatus>): List<AgentTask>
    suspend fun insert(task: AgentTask)
    suspend fun update(task: AgentTask)
    suspend fun delete(taskId: String)
    suspend fun updateStatus(taskId: String, status: AgentTaskStatus, error: String? = null)
    suspend fun updateProgress(taskId: String, progress: Float)
    suspend fun updateResult(taskId: String, result: String, status: AgentTaskStatus)
    suspend fun updateWorkerId(taskId: String, workerId: String)
    suspend fun updateRetryCount(taskId: String, count: Int)
    suspend fun updateLastRunAt(taskId: String, timestamp: Long)
    suspend fun updateNextRunAt(taskId: String, timestamp: Long)
}
