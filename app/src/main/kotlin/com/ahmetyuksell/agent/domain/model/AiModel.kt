package com.ahmetyuksell.agent.domain.model

data class AiModel(
    val id: String,
    val displayName: String,
    val provider: String,
    val contextLength: Int,
    val supportsVision: Boolean = false,
    val supportsFunctionCalling: Boolean = false,
    val pricingInputPer1k: Double = 0.0,
    val pricingOutputPer1k: Double = 0.0,
    val isEnabled: Boolean = true,
    val displayOrder: Int = 0
)
