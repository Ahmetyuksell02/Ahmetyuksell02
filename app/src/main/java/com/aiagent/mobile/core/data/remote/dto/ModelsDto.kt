package com.aiagent.mobile.core.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ModelsResponseDto(
    @Json(name = "data") val data: List<ModelDto>
)

@JsonClass(generateAdapter = true)
data class ModelDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "description") val description: String?,
    @Json(name = "context_length") val contextLength: Int?,
    @Json(name = "pricing") val pricing: ModelPricingDto?
)

@JsonClass(generateAdapter = true)
data class ModelPricingDto(
    @Json(name = "prompt") val prompt: String?,
    @Json(name = "completion") val completion: String?
)
