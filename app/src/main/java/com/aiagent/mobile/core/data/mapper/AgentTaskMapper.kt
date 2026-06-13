package com.aiagent.mobile.core.data.mapper

import com.aiagent.mobile.core.data.local.entity.AgentTaskEntity
import com.aiagent.mobile.core.domain.agent.AgentPriority
import com.aiagent.mobile.core.domain.model.AgentTask
import com.aiagent.mobile.core.domain.model.AgentTaskStatus
import com.aiagent.mobile.core.domain.model.AgentTaskType
import com.aiagent.mobile.core.domain.model.TriggerType

fun AgentTaskEntity.toDomain() = AgentTask(
    id = id,
    title = title,
    description = description,
    prompt = prompt,
    taskType = runCatching { AgentTaskType.valueOf(taskType) }.getOrDefault(AgentTaskType.CUSTOM),
    status = runCatching { AgentTaskStatus.valueOf(status) }.getOrDefault(AgentTaskStatus.PENDING),
    progress = progress,
    isPeriodic = isPeriodic,
    intervalMinutes = intervalMinutes,
    scheduledAt = scheduledAt,
    lastRunAt = lastRunAt,
    nextRunAt = nextRunAt,
    result = result,
    error = error,
    workerId = workerId,
    retryCount = retryCount,
    maxRetries = maxRetries,
    triggerType = runCatching { TriggerType.valueOf(triggerType) }.getOrDefault(TriggerType.MANUAL),
    priority = runCatching { AgentPriority.valueOf(priority) }.getOrDefault(AgentPriority.NORMAL),
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun AgentTask.toEntity() = AgentTaskEntity(
    id = id,
    title = title,
    description = description,
    prompt = prompt,
    taskType = taskType.name,
    status = status.name,
    progress = progress,
    isPeriodic = isPeriodic,
    intervalMinutes = intervalMinutes,
    scheduledAt = scheduledAt,
    lastRunAt = lastRunAt,
    nextRunAt = nextRunAt,
    result = result,
    error = error,
    workerId = workerId,
    retryCount = retryCount,
    maxRetries = maxRetries,
    triggerType = triggerType.name,
    priority = priority.name,
    createdAt = createdAt,
    updatedAt = updatedAt
)
