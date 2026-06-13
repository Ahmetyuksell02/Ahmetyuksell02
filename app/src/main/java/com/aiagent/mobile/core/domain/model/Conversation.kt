package com.aiagent.mobile.core.domain.model

data class Conversation(
    val id: String,
    val title: String,
    val modelId: String,
    val modelName: String,
    val createdAt: Long,
    val updatedAt: Long,
    val lastMessage: String,
    val isPinned: Boolean = false,
    val folderId: String? = null,
    val systemPrompt: String? = null
)
