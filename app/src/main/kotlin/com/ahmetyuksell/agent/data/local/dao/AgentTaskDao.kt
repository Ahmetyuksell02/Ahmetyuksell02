package com.ahmetyuksell.agent.data.local.dao

import androidx.room.*
import com.ahmetyuksell.agent.data.local.entity.AgentTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AgentTaskDao {

    @Query("SELECT * FROM agent_tasks WHERE agent_id = :agentId ORDER BY started_at DESC")
    fun getTasksForAgent(agentId: String): Flow<List<AgentTaskEntity>>

    @Query("SELECT * FROM agent_tasks WHERE status IN ('RUNNING', 'PENDING') ORDER BY started_at DESC")
    fun getRunningTasks(): Flow<List<AgentTaskEntity>>

    @Query("SELECT * FROM agent_tasks ORDER BY started_at DESC")
    fun getAllTasks(): Flow<List<AgentTaskEntity>>

    @Query("SELECT * FROM agent_tasks WHERE id = :id LIMIT 1")
    suspend fun getTask(id: String): AgentTaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(entity: AgentTaskEntity)

    @Update
    suspend fun updateTask(entity: AgentTaskEntity)

    @Query("UPDATE agent_tasks SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("""
        UPDATE agent_tasks
        SET status = 'COMPLETED', result_text = :result, steps_json = :steps, completed_at = :completedAt
        WHERE id = :id
    """)
    suspend fun updateResult(id: String, result: String, steps: String, completedAt: Long)

    @Query("""
        UPDATE agent_tasks
        SET status = 'FAILED', error_message = :error, completed_at = :completedAt
        WHERE id = :id
    """)
    suspend fun updateError(id: String, error: String, completedAt: Long)

    @Query("UPDATE agent_tasks SET status = 'CANCELLED', completed_at = :completedAt WHERE id = :id")
    suspend fun cancelTask(id: String, completedAt: Long)

    // Called once on boot to repair tasks that were RUNNING/PENDING when the app last crashed.
    @Query("""
        UPDATE agent_tasks
        SET status = 'FAILED',
            error_message = 'Task interrupted: app was terminated while this task was running',
            completed_at = :now
        WHERE status = 'RUNNING' OR status = 'PENDING'
    """)
    suspend fun resetOrphanedTasks(now: Long): Int
}
