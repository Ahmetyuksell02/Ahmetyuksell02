package com.ahmetyuksell.agent.data.mapper

import com.ahmetyuksell.agent.data.local.entity.MessageEntity
import com.ahmetyuksell.agent.domain.model.Message
import com.ahmetyuksell.agent.domain.model.MessageRole

fun MessageEntity.toDomain() = Message(
    id = id,
    conversationId = conversationId,
    role = MessageRole.valueOf(role),
    content = content,
    modelId = modelId,
    timestamp = timestamp,
    tokenCount = tokenCount,
    isStreaming = isStreaming,
    toolCallId = toolCallId,
    toolCallName = toolCallName,
    toolCallArgsJson = toolCallArgsJson,
    toolResultJson = toolResultJson,
    fileIds = fileIdsJson?.let { json ->
        json.removeSurrounding("[", "]")
            .split(",")
            .map { it.trim().removeSurrounding("\"") }
            .filter { it.isNotBlank() }
    } ?: emptyList()
)

fun Message.toEntity() = MessageEntity(
    id = id,
    conversationId = conversationId,
    role = role.name,
    content = content,
    modelId = modelId,
    timestamp = timestamp,
    tokenCount = tokenCount,
    isStreaming = isStreaming,
    toolCallId = toolCallId,
    toolCallName = toolCallName,
    toolCallArgsJson = toolCallArgsJson,
    toolResultJson = toolResultJson,
    fileIdsJson = if (fileIds.isEmpty()) null
    else "[${fileIds.joinToString(",") { "\"$it\"" }}]"
)
