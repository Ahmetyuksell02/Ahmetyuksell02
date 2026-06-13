package com.ahmetyuksell.agent.data.mapper

import com.ahmetyuksell.agent.data.local.entity.ConversationEntity
import com.ahmetyuksell.agent.domain.model.Conversation

fun ConversationEntity.toDomain() = Conversation(
    id = id,
    title = title,
    modelId = modelId,
    systemPrompt = systemPrompt,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isArchived = isArchived,
    metadataJson = metadataJson
)

fun Conversation.toEntity() = ConversationEntity(
    id = id,
    title = title,
    modelId = modelId,
    systemPrompt = systemPrompt,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isArchived = isArchived,
    metadataJson = metadataJson
)
