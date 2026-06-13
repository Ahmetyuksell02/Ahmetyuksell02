package com.aiagent.mobile.core.domain.model

data class AgentTask(
    val id: String,
    val title: String,
    val description: String,
    val prompt: String,
    val taskType: AgentTaskType,
    val status: AgentTaskStatus,
    val progress: Float = 0f,
    val isPeriodic: Boolean = false,
    val intervalMinutes: Long = 1440L,
    val scheduledAt: Long? = null,
    val lastRunAt: Long? = null,
    val nextRunAt: Long? = null,
    val result: String? = null,
    val error: String? = null,
    val workerId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class AgentTaskType {
    NEWS_SUMMARY, PRICE_MONITOR, JOB_MONITOR,
    FILE_ANALYSIS, RESEARCH, WEB_INVESTIGATION, CUSTOM
}

enum class AgentTaskStatus { PENDING, RUNNING, COMPLETED, FAILED, PAUSED }
