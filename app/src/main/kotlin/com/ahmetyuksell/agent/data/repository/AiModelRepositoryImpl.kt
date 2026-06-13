package com.ahmetyuksell.agent.data.repository

import com.ahmetyuksell.agent.data.local.dao.AiModelDao
import com.ahmetyuksell.agent.data.mapper.toDomain
import com.ahmetyuksell.agent.data.mapper.toEntity
import com.ahmetyuksell.agent.data.remote.api.OpenRouterApi
import com.ahmetyuksell.agent.domain.model.AiModel
import com.ahmetyuksell.agent.domain.model.ApiResult
import com.ahmetyuksell.agent.domain.repository.AiModelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiModelRepositoryImpl @Inject constructor(
    private val dao: AiModelDao,
    private val api: OpenRouterApi
) : AiModelRepository {

    override fun getEnabledModels(): Flow<List<AiModel>> =
        dao.getEnabledModels().map { list -> list.map { it.toDomain() } }

    override fun getAllModels(): Flow<List<AiModel>> =
        dao.getAllModels().map { list -> list.map { it.toDomain() } }

    override suspend fun getModel(id: String): AiModel? =
        dao.getModel(id)?.toDomain()

    override suspend fun upsertModels(models: List<AiModel>) {
        dao.upsertModels(models.map { it.toEntity() })
    }

    override suspend fun setModelEnabled(id: String, enabled: Boolean) {
        dao.setEnabled(id, enabled)
    }

    override suspend fun setModelOrder(id: String, order: Int) {
        dao.setOrder(id, order)
    }

    override suspend fun syncModelsFromApi(): ApiResult<Int> {
        return try {
            val response = api.listModels()
            val models = response.data.map { it.toDomain() }
            dao.upsertModels(models.map { it.toEntity() })
            ApiResult.Success(models.size)
        } catch (e: HttpException) {
            when (e.code()) {
                401 -> ApiResult.AuthError
                429 -> ApiResult.RateLimitError(60_000L)
                else -> ApiResult.ApiError(e.code(), e.message())
            }
        } catch (e: IOException) {
            ApiResult.NetworkError(e.message ?: "Network error")
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error syncing models")
            ApiResult.ApiError(-1, e.message ?: "Unknown error")
        }
    }
}
