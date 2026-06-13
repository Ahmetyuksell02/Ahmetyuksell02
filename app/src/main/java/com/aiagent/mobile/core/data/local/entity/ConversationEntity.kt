package com.aiagent.mobile.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val modelId: String,
    val modelName: String,
    val createdAt: Long,
    val updatedAt: Long,
    val lastMessage: String,
    val isPinned: Boolean,
    val folderId: String?,
    val systemPrompt: String?
)
