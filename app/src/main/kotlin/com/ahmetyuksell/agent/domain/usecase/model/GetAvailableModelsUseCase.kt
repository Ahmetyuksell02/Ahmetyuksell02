package com.ahmetyuksell.agent.domain.usecase.model

import com.ahmetyuksell.agent.domain.model.AiModel
import com.ahmetyuksell.agent.domain.model.ApiResult
import com.ahmetyuksell.agent.domain.repository.AiModelRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAvailableModelsUseCase @Inject constructor(
    private val aiModelRepository: AiModelRepository
) {
    fun getEnabled(): Flow<List<AiModel>> = aiModelRepository.getEnabledModels()
    fun getAll(): Flow<List<AiModel>> = aiModelRepository.getAllModels()
    suspend fun sync(): ApiResult<Int> = aiModelRepository.syncModelsFromApi()
}
