package com.aiagent.mobile.core.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aiagent.mobile.core.data.scheduler.AgentScheduler
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

@HiltWorker
class RescheduleAgentsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val agentScheduler: AgentScheduler
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            Timber.i("RescheduleAgentsWorker: rescheduling all pending agent tasks")
            agentScheduler.rescheduleAllPending()
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "RescheduleAgentsWorker: failed to reschedule tasks")
            Result.retry()
        }
    }
}
