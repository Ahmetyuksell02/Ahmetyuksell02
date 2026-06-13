package com.ahmetyuksell.agent.data.mapper

import com.ahmetyuksell.agent.data.local.entity.FileEntity
import com.ahmetyuksell.agent.domain.model.AttachedFile

fun FileEntity.toDomain() = AttachedFile(
    id = id,
    conversationId = conversationId,
    localUri = localUri,
    name = name,
    mimeType = mimeType,
    sizeBytes = sizeBytes,
    sha256Hash = sha256Hash,
    openrouterFileId = openrouterFileId,
    uploadedAt = uploadedAt
)

fun AttachedFile.toEntity() = FileEntity(
    id = id,
    conversationId = conversationId,
    localUri = localUri,
    name = name,
    mimeType = mimeType,
    sizeBytes = sizeBytes,
    sha256Hash = sha256Hash,
    openrouterFileId = openrouterFileId,
    uploadedAt = uploadedAt
)
