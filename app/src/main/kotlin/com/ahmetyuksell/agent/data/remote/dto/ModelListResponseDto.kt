package com.ahmetyuksell.agent.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ModelListResponseDto(
    val data: List<ModelDto>
)

@Serializable
data class ModelDto(
    val id: String,
    val name: String,
    val description: String? = null,
    @SerialName("context_length") val contextLength: Int = 4096,
    val architecture: ModelArchitectureDto? = null,
    val pricing: ModelPricingDto? = null,
    @SerialName("top_provider") val topProvider: TopProviderDto? = null
)

@Serializable
data class ModelArchitectureDto(
    val modality: String? = null,
    val tokenizer: String? = null,
    @SerialName("instruct_type") val instructType: String? = null
)

@Serializable
data class ModelPricingDto(
    val prompt: String? = null,
    val completion: String? = null
)

@Serializable
data class TopProviderDto(
    @SerialName("context_length") val contextLength: Int? = null,
    @SerialName("max_completion_tokens") val maxCompletionTokens: Int? = null,
    @SerialName("is_moderated") val isModerated: Boolean? = null
)
