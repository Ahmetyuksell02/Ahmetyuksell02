package com.ahmetyuksell.agent.domain.model

data class Message(
    val id: String,
    val conversationId: String,
    val role: MessageRole,
    val content: String,
    val modelId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val tokenCount: Int? = null,
    val isStreaming: Boolean = false,
    val toolCallId: String? = null,
    val toolCallName: String? = null,
    val toolCallArgsJson: String? = null,
    val toolResultJson: String? = null,
    val fileIds: List<String> = emptyList()
)

enum class MessageRole {
    USER, ASSISTANT, SYSTEM, TOOL;

    fun apiValue(): String = name.lowercase()
}
