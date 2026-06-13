package com.ahmetyuksell.agent.domain.model

data class AttachedFile(
    val id: String,
    val conversationId: String? = null,
    val localUri: String,
    val name: String,
    val mimeType: String,
    val sizeBytes: Long,
    val sha256Hash: String? = null,
    val openrouterFileId: String? = null,
    val uploadedAt: Long
)
