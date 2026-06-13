package com.aiagent.mobile.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aiagent.mobile.core.data.local.entity.AgentExecutionLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AgentExecutionLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: AgentExecutionLogEntity)

    @Query("SELECT * FROM agent_execution_logs WHERE taskId = :taskId ORDER BY timestamp ASC")
    fun getLogsForTask(taskId: String): Flow<List<AgentExecutionLogEntity>>

    @Query("SELECT * FROM agent_execution_logs WHERE taskId = :taskId ORDER BY timestamp ASC")
    suspend fun getLogsOnce(taskId: String): List<AgentExecutionLogEntity>

    @Query("""
        SELECT * FROM agent_execution_logs
        WHERE taskId = :taskId AND stepType = 'CHECKPOINT'
        ORDER BY timestamp DESC LIMIT 1
    """)
    suspend fun getLatestCheckpoint(taskId: String): AgentExecutionLogEntity?

    @Query("SELECT COUNT(*) FROM agent_execution_logs WHERE taskId = :taskId AND isError = 1")
    suspend fun getErrorCount(taskId: String): Int

    @Query("""
        SELECT * FROM agent_execution_logs
        WHERE isError = 1
        ORDER BY timestamp DESC LIMIT :limit
    """)
    fun getRecentErrors(limit: Int = 50): Flow<List<AgentExecutionLogEntity>>

    @Query("DELETE FROM agent_execution_logs WHERE taskId = :taskId")
    suspend fun deleteLogsForTask(taskId: String)

    @Query("""
        DELETE FROM agent_execution_logs
        WHERE timestamp < :cutoffTimestamp
    """)
    suspend fun deleteOlderThan(cutoffTimestamp: Long)
}
