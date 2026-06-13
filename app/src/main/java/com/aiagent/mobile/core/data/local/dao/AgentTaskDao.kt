package com.aiagent.mobile.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aiagent.mobile.core.data.local.entity.AgentTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AgentTaskDao {

    @Query("SELECT * FROM agent_tasks ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<AgentTaskEntity>>

    @Query("SELECT * FROM agent_tasks WHERE id = :id")
    fun getById(id: String): Flow<AgentTaskEntity?>

    @Query("SELECT * FROM agent_tasks WHERE id = :id LIMIT 1")
    suspend fun getByIdOnce(id: String): AgentTaskEntity?

    @Query("SELECT * FROM agent_tasks WHERE status = :status")
    fun getByStatus(status: String): Flow<List<AgentTaskEntity>>

    @Query("SELECT * FROM agent_tasks WHERE status IN (:statuses)")
    fun getByStatuses(statuses: List<String>): Flow<List<AgentTaskEntity>>

    @Query("SELECT * FROM agent_tasks WHERE status IN (:statuses)")
    suspend fun getByStatusesOnce(statuses: List<String>): List<AgentTaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AgentTaskEntity)

    @Update
    suspend fun update(entity: AgentTaskEntity)

    @Query("DELETE FROM agent_tasks WHERE id = :id")
    suspend fun delete(id: String)

    @Query("UPDATE agent_tasks SET status = :status, error = :error, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, error: String?, updatedAt: Long)

    @Query("UPDATE agent_tasks SET progress = :progress, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateProgress(id: String, progress: Float, updatedAt: Long)

    @Query("UPDATE agent_tasks SET result = :result, status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateResult(id: String, result: String, status: String, updatedAt: Long)

    @Query("UPDATE agent_tasks SET workerId = :workerId WHERE id = :id")
    suspend fun updateWorkerId(id: String, workerId: String)

    @Query("UPDATE agent_tasks SET retryCount = :retryCount, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateRetryCount(id: String, retryCount: Int, updatedAt: Long)

    @Query("UPDATE agent_tasks SET lastRunAt = :lastRunAt, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateLastRunAt(id: String, lastRunAt: Long, updatedAt: Long)

    @Query("UPDATE agent_tasks SET nextRunAt = :nextRunAt, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateNextRunAt(id: String, nextRunAt: Long, updatedAt: Long)
}
