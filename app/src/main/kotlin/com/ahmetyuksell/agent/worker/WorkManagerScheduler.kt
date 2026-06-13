package com.ahmetyuksell.agent.worker

import android.content.Context
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkManagerScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Lazy to avoid triggering WorkManager init before Application.onCreate completes
    private val workManager by lazy { WorkManager.getInstance(context) }

    fun scheduleAgentTask(taskId: String) {
        val request = OneTimeWorkRequestBuilder<AgentTaskWorker>()
            .setConstraints(networkConstraints())
            .setInputData(workDataOf(AgentTaskWorker.KEY_TASK_ID to taskId))
            .addTag(taskId)
            .addTag(TAG_AGENT)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

        workManager.enqueueUniqueWork("agent_$taskId", ExistingWorkPolicy.KEEP, request)
    }

    fun scheduleFileAnalysis(fileId: String) {
        val request = OneTimeWorkRequestBuilder<FileAnalysisWorker>()
            .setConstraints(networkConstraints())
            .setInputData(workDataOf(FileAnalysisWorker.KEY_FILE_ID to fileId))
            .addTag(TAG_FILE)
            .build()

        workManager.enqueueUniqueWork("file_$fileId", ExistingWorkPolicy.KEEP, request)
    }

    fun scheduleModelSync() {
        val request = PeriodicWorkRequestBuilder<ModelSyncWorker>(24, TimeUnit.HOURS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.UNMETERED).build())
            .addTag(TAG_SYNC)
            .build()

        workManager.enqueueUniquePeriodicWork(WORK_MODEL_SYNC, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    fun cancelAgentTask(taskId: String) {
        workManager.cancelAllWorkByTag(taskId)
    }

    fun cancelAllAgentTasks() {
        workManager.cancelAllWorkByTag(TAG_AGENT)
    }

    private fun networkConstraints() = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    companion object {
        const val TAG_AGENT = "agent_work"
        const val TAG_FILE = "file_work"
        const val TAG_SYNC = "sync_work"
        const val WORK_MODEL_SYNC = "model_sync_periodic"
    }
}
