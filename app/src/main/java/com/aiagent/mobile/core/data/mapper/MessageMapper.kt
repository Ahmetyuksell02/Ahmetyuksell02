package com.aiagent.mobile.core.data.mapper

import com.aiagent.mobile.core.data.local.entity.MessageEntity
import com.aiagent.mobile.core.domain.model.AttachmentType
import com.aiagent.mobile.core.domain.model.Message
import com.aiagent.mobile.core.domain.model.MessageRole

fun MessageEntity.toDomain() = Message(
    id = id,
    conversationId = conversationId,
    content = content,
    role = MessageRole.fromApiValue(role),
    timestamp = timestamp,
    isError = isError,
    attachmentUri = attachmentUri,
    attachmentType = attachmentType?.let { runCatching { AttachmentType.valueOf(it) }.getOrNull() }
)

fun Message.toEntity() = MessageEntity(
    id = id,
    conversationId = conversationId,
    content = content,
    role = role.apiValue,
    timestamp = timestamp,
    isError = isError,
    attachmentUri = attachmentUri,
    attachmentType = attachmentType?.name
)
