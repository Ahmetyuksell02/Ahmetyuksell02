package com.aiagent.mobile.core.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "agent_execution_logs",
    foreignKeys = [
        ForeignKey(
            entity = AgentTaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("taskId")]
)
data class AgentExecutionLogEntity(
    @PrimaryKey val id: String,
    val taskId: String,
    val timestamp: Long,
    val stepName: String,
    val stepType: String,        // TOOL_CALL | LLM_CALL | PROGRESS | CHECKPOINT | ERROR | WORKER_START | WORKER_END
    val inputSummary: String?,   // first 300 chars of input
    val outputSummary: String?,  // first 300 chars of output
    val checkpointData: String?, // full intermediate data for CHECKPOINT steps (can be large)
    val durationMs: Long = 0L,
    val toolUsed: String? = null,
    val tokensEstimated: Int? = null,
    val isError: Boolean = false,
    val errorMessage: String? = null
)

// Step type constants
object LogStepType {
    const val WORKER_START = "WORKER_START"
    const val WORKER_END = "WORKER_END"
    const val TOOL_CALL = "TOOL_CALL"
    const val LLM_CALL = "LLM_CALL"
    const val CHECKPOINT = "CHECKPOINT"
    const val PROGRESS = "PROGRESS"
    const val ERROR = "ERROR"
}
