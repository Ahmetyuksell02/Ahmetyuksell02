package com.ahmetyuksell.agent.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "agent_tasks",
    foreignKeys = [
        ForeignKey(
            entity = AgentEntity::class,
            parentColumns = ["id"],
            childColumns = ["agent_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("agent_id"), Index("status")]
)
data class AgentTaskEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "agent_id") val agentId: String,
    @ColumnInfo(name = "conversation_id") val conversationId: String,
    val status: String,
    @ColumnInfo(name = "input_text") val inputText: String,
    @ColumnInfo(name = "result_text") val resultText: String?,
    @ColumnInfo(name = "error_message") val errorMessage: String?,
    @ColumnInfo(name = "steps_json") val stepsJson: String?,
    @ColumnInfo(name = "started_at") val startedAt: Long?,
    @ColumnInfo(name = "completed_at") val completedAt: Long?,
    @ColumnInfo(name = "retry_count") val retryCount: Int = 0
)
