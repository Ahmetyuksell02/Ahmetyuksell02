package com.ahmetyuksell.agent.data.mapper

import com.ahmetyuksell.agent.data.local.entity.AgentEntity
import com.ahmetyuksell.agent.data.local.entity.AgentTaskEntity
import com.ahmetyuksell.agent.domain.model.Agent
import com.ahmetyuksell.agent.domain.model.AgentTask
import com.ahmetyuksell.agent.domain.model.AgentTaskStatus

fun AgentEntity.toDomain() = Agent(
    id = id,
    name = name,
    description = description,
    systemPrompt = systemPrompt,
    modelId = modelId,
    toolsJson = toolsJson,
    createdAt = createdAt,
    isActive = isActive
)

fun Agent.toEntity() = AgentEntity(
    id = id,
    name = name,
    description = description,
    systemPrompt = systemPrompt,
    modelId = modelId,
    toolsJson = toolsJson,
    createdAt = createdAt,
    isActive = isActive
)

fun AgentTaskEntity.toDomain() = AgentTask(
    id = id,
    agentId = agentId,
    conversationId = conversationId,
    status = AgentTaskStatus.valueOf(status),
    inputText = inputText,
    resultText = resultText,
    errorMessage = errorMessage,
    stepsJson = stepsJson,
    startedAt = startedAt,
    completedAt = completedAt,
    retryCount = retryCount
)

fun AgentTask.toEntity() = AgentTaskEntity(
    id = id,
    agentId = agentId,
    conversationId = conversationId,
    status = status.name,
    inputText = inputText,
    resultText = resultText,
    errorMessage = errorMessage,
    stepsJson = stepsJson,
    startedAt = startedAt,
    completedAt = completedAt,
    retryCount = retryCount
)
