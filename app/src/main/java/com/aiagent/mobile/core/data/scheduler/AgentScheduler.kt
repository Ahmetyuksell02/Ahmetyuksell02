package com.aiagent.mobile.core.data.scheduler

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.aiagent.mobile.core.common.Constants
import com.aiagent.mobile.core.data.worker.AgentWorker
import com.aiagent.mobile.core.domain.model.AgentTask
import com.aiagent.mobile.core.domain.model.AgentTaskStatus
import com.aiagent.mobile.core.domain.repository.IAgentTaskRepository
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgentScheduler @Inject constructor(
    private val workManager: WorkManager,
    private val agentTaskRepository: IAgentTaskRepository
) {
    private val networkConstraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    fun schedule(task: AgentTask) {
        val request = buildWorkRequest(task, 0L)
        workManager.enqueueUniqueWork(
            task.id,
            ExistingWorkPolicy.KEEP,
            request
        )
        Timber.d("AgentScheduler: scheduled task ${task.id} (${task.title})")
    }

    fun scheduleDelayed(task: AgentTask, delayMs: Long) {
        val request = buildWorkRequest(task, delayMs)
        workManager.enqueueUniqueWork(
            task.id,
            ExistingWorkPolicy.REPLACE,
            request
        )
        Timber.d("AgentScheduler: scheduled task ${task.id} with delay ${delayMs}ms")
    }

    fun cancel(taskId: String) {
        workManager.cancelUniqueWork(taskId)
        Timber.d("AgentScheduler: cancelled task $taskId")
    }

    fun cancelAll() {
        workManager.cancelAllWorkByTag(Constants.AGENT_TASK_WORKER_TAG)
        Timber.d("AgentScheduler: cancelled all agent tasks")
    }

    suspend fun rescheduleAllPending() {
        val statuses = listOf(
            AgentTaskStatus.PENDING,
            AgentTaskStatus.SCHEDULED,
            AgentTaskStatus.RETRYING
        )
        val pendingTasks = agentTaskRepository.getByStatusesOnce(statuses)
        pendingTasks.forEach { task ->
            schedule(task)
            Timber.d("AgentScheduler: rescheduled pending task ${task.id}")
        }

        // Tasks interrupted mid-run → reset to PENDING and reschedule
        val runningTasks = agentTaskRepository.getByStatusesOnce(listOf(AgentTaskStatus.RUNNING))
        runningTasks.forEach { task ->
            agentTaskRepository.updateStatus(task.id, AgentTaskStatus.PENDING)
            schedule(task)
            Timber.d("AgentScheduler: reset and rescheduled interrupted task ${task.id}")
        }

        Timber.i("AgentScheduler: rescheduled ${pendingTasks.size + runningTasks.size} tasks after boot")
    }

    fun isEnqueued(taskId: String): Boolean {
        val workInfos = workManager.getWorkInfosForUniqueWork(taskId).get()
        return workInfos.any { !it.state.isFinished }
    }

    private fun buildWorkRequest(task: AgentTask, initialDelayMs: Long) =
        OneTimeWorkRequestBuilder<AgentWorker>()
            .setInputData(workDataOf(Constants.KEY_TASK_ID to task.id))
            .setConstraints(networkConstraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                Constants.AGENT_BACKOFF_DELAY_SECONDS,
                TimeUnit.SECONDS
            )
            .addTag(Constants.AGENT_TASK_WORKER_TAG)
            .addTag(task.id)
            .apply {
                if (initialDelayMs > 0) setInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS)
            }
            .build()
}
