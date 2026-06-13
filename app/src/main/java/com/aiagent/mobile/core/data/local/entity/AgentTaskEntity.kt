package com.aiagent.mobile.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "agent_tasks")
data class AgentTaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val prompt: String,
    val taskType: String,
    val status: String,
    val progress: Float,
    val isPeriodic: Boolean,
    val intervalMinutes: Long,
    val scheduledAt: Long?,
    val lastRunAt: Long?,
    val nextRunAt: Long?,
    val result: String?,
    val error: String?,
    val workerId: String?,
    val retryCount: Int = 0,
    val maxRetries: Int = 3,
    val triggerType: String = "MANUAL",
    val createdAt: Long,
    val updatedAt: Long
)
