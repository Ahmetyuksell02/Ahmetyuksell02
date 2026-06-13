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
    val retryCount: Int = 0,
    val maxRetries: Int = 3,
    val triggerType: TriggerType = TriggerType.MANUAL,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class AgentTaskType {
    NEWS_SUMMARY, PRICE_MONITOR, JOB_MONITOR,
    FILE_ANALYSIS, RESEARCH, WEB_INVESTIGATION, CUSTOM
}

enum class AgentTaskStatus {
    PENDING, SCHEDULED, RUNNING, PAUSED, COMPLETED, FAILED, RETRYING
}

enum class TriggerType {
    MANUAL, TIME_BASED, EVENT_BASED, CONDITION_BASED
}
