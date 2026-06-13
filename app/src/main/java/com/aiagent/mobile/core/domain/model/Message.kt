package com.aiagent.mobile.core.domain.model

data class Message(
    val id: String,
    val conversationId: String,
    val content: String,
    val role: MessageRole,
    val timestamp: Long,
    val isError: Boolean = false,
    val attachmentUri: String? = null,
    val attachmentType: AttachmentType? = null
)

enum class MessageRole(val apiValue: String) {
    USER("user"),
    ASSISTANT("assistant"),
    SYSTEM("system");

    companion object {
        fun fromApiValue(value: String) = entries.find { it.apiValue == value } ?: USER
    }
}

enum class AttachmentType { IMAGE, PDF, DOCX, TXT }
