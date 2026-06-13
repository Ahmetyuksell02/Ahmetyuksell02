package com.ahmetyuksell.agent.domain.model

data class Conversation(
    val id: String,
    val title: String,
    val modelId: String,
    val systemPrompt: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val isArchived: Boolean = false,
    val metadataJson: String? = null
)
