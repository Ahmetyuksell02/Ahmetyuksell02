package com.aiagent.mobile.core.data.repository

import com.aiagent.mobile.core.common.Resource
import com.aiagent.mobile.core.data.mapper.toDomain
import com.aiagent.mobile.core.data.remote.api.OpenRouterApi
import com.aiagent.mobile.core.domain.model.AIModel
import com.aiagent.mobile.core.domain.repository.IModelRepository
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelRepositoryImpl @Inject constructor(
    private val api: OpenRouterApi
) : IModelRepository {

    // In-memory cache — replaced by a DataStore-backed cache in a future iteration
    private var cachedModels: List<AIModel>? = null

    override suspend fun getModels(): Resource<List<AIModel>> {
        cachedModels?.let { return Resource.Success(it) }
        return refreshModels()
    }

    override suspend fun refreshModels(): Resource<List<AIModel>> {
        return try {
            val models = api.getModels().data
                .map { it.toDomain() }
                .sortedBy { it.name }
            cachedModels = models
            Resource.Success(models)
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch models")
            Resource.Error(e.message ?: "Failed to load models", e)
        }
    }
}
