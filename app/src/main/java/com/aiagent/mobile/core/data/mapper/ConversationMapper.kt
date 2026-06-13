package com.aiagent.mobile.core.data.mapper

import com.aiagent.mobile.core.data.local.entity.ConversationEntity
import com.aiagent.mobile.core.domain.model.Conversation

fun ConversationEntity.toDomain() = Conversation(
    id = id,
    title = title,
    modelId = modelId,
    modelName = modelName,
    createdAt = createdAt,
    updatedAt = updatedAt,
    lastMessage = lastMessage,
    isPinned = isPinned,
    folderId = folderId,
    systemPrompt = systemPrompt
)

fun Conversation.toEntity() = ConversationEntity(
    id = id,
    title = title,
    modelId = modelId,
    modelName = modelName,
    createdAt = createdAt,
    updatedAt = updatedAt,
    lastMessage = lastMessage,
    isPinned = isPinned,
    folderId = folderId,
    systemPrompt = systemPrompt
)
