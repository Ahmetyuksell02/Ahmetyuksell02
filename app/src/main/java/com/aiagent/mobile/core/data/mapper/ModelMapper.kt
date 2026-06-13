package com.aiagent.mobile.core.data.mapper

import com.aiagent.mobile.core.data.remote.dto.ModelDto
import com.aiagent.mobile.core.domain.model.AIModel
import com.aiagent.mobile.core.domain.model.ModelPricing

fun ModelDto.toDomain() = AIModel(
    id = id,
    name = name.ifBlank { id },
    description = description.orEmpty(),
    contextLength = contextLength ?: 4096,
    pricing = ModelPricing(
        promptPerToken = pricing?.prompt ?: "0",
        completionPerToken = pricing?.completion ?: "0"
    )
)
