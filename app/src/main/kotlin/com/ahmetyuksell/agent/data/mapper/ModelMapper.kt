package com.ahmetyuksell.agent.data.mapper

import com.ahmetyuksell.agent.data.local.entity.AiModelEntity
import com.ahmetyuksell.agent.data.remote.dto.ModelDto
import com.ahmetyuksell.agent.domain.model.AiModel

fun AiModelEntity.toDomain() = AiModel(
    id = id,
    displayName = displayName,
    provider = provider,
    contextLength = contextLength,
    supportsVision = supportsVision,
    supportsFunctionCalling = supportsFunctionCalling,
    pricingInputPer1k = pricingInputPer1k,
    pricingOutputPer1k = pricingOutputPer1k,
    isEnabled = isEnabled,
    displayOrder = displayOrder
)

fun AiModel.toEntity() = AiModelEntity(
    id = id,
    displayName = displayName,
    provider = provider,
    contextLength = contextLength,
    supportsVision = supportsVision,
    supportsFunctionCalling = supportsFunctionCalling,
    pricingInputPer1k = pricingInputPer1k,
    pricingOutputPer1k = pricingOutputPer1k,
    isEnabled = isEnabled,
    displayOrder = displayOrder
)

fun ModelDto.toDomain(): AiModel {
    val providerId = id.substringBefore("/")
    val supportsVision = architecture?.modality?.contains("image") == true
    val supportsTools = architecture?.modality?.contains("text") == true
    val inputPrice = pricing?.prompt?.toDoubleOrNull() ?: 0.0
    val outputPrice = pricing?.completion?.toDoubleOrNull() ?: 0.0

    return AiModel(
        id = id,
        displayName = name,
        provider = providerId,
        contextLength = topProvider?.contextLength ?: contextLength,
        supportsVision = supportsVision,
        supportsFunctionCalling = supportsTools,
        pricingInputPer1k = inputPrice * 1000,
        pricingOutputPer1k = outputPrice * 1000,
        isEnabled = true,
        displayOrder = 0
    )
}
