package com.aiagent.mobile.core.domain.model

data class AIModel(
    val id: String,
    val name: String,
    val description: String,
    val contextLength: Int,
    val pricing: ModelPricing
)

data class ModelPricing(
    val promptPerToken: String,
    val completionPerToken: String
)
