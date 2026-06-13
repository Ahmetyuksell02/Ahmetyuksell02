package com.ahmetyuksell.agent.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ahmetyuksell.agent.domain.model.ApiResult
import com.ahmetyuksell.agent.domain.repository.AiModelRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

@HiltWorker
class ModelSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val aiModelRepository: AiModelRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return when (val result = aiModelRepository.syncModelsFromApi()) {
            is ApiResult.Success -> {
                Timber.d("Model sync: ${result.data} models updated")
                Result.success()
            }
            is ApiResult.AuthError -> {
                Timber.w("Model sync failed: auth error")
                Result.failure()
            }
            is ApiResult.NetworkError -> {
                Timber.w("Model sync network error: ${result.message}")
                if (runAttemptCount < 3) Result.retry() else Result.failure()
            }
            is ApiResult.RateLimitError -> {
                Timber.w("Model sync rate limited")
                Result.retry()
            }
            is ApiResult.ApiError -> {
                Timber.e("Model sync API error ${result.httpCode}: ${result.message}")
                Result.failure()
            }
        }
    }
}
