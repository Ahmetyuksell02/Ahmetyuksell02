package com.ahmetyuksell.agent.domain.repository

import com.ahmetyuksell.agent.domain.model.AiModel
import com.ahmetyuksell.agent.domain.model.ApiResult
import kotlinx.coroutines.flow.Flow

interface AiModelRepository {
    fun getEnabledModels(): Flow<List<AiModel>>
    fun getAllModels(): Flow<List<AiModel>>
    suspend fun getModel(id: String): AiModel?
    suspend fun upsertModels(models: List<AiModel>)
    suspend fun setModelEnabled(id: String, enabled: Boolean)
    suspend fun setModelOrder(id: String, order: Int)
    suspend fun syncModelsFromApi(): ApiResult<Int>
}
