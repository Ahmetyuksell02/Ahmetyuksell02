package com.ahmetyuksell.agent.domain.model

data class Agent(
    val id: String,
    val name: String,
    val description: String,
    val systemPrompt: String,
    val modelId: String,
    val toolsJson: String,
    val createdAt: Long,
    val isActive: Boolean = true
)
