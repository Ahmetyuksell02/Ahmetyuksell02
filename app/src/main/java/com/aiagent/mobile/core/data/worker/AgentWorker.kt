package com.aiagent.mobile.core.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.aiagent.mobile.core.common.Constants
import com.aiagent.mobile.core.data.agent.AgentDispatcher
import com.aiagent.mobile.core.data.notification.AgentNotificationManager
import com.aiagent.mobile.core.data.scheduler.AgentScheduler
import com.aiagent.mobile.core.domain.agent.AgentEvent
import com.aiagent.mobile.core.domain.agent.AgentEventBus
import com.aiagent.mobile.core.domain.agent.AgentExecutionResult
import com.aiagent.mobile.core.domain.model.AgentTask
import com.aiagent.mobile.core.domain.model.AgentTaskStatus
import com.aiagent.mobile.core.domain.repository.IAgentTaskRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber

@HiltWorker
class AgentWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val agentTaskRepository: IAgentTaskRepository,
    private val agentDispatcher: AgentDispatcher,
    private val agentEventBus: AgentEventBus,
    private val agentNotificationManager: AgentNotificationManager,
    private val agentScheduler: AgentScheduler
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val taskId = inputData.getString(Constants.KEY_TASK_ID)
            ?: return Result.failure(workDataOf(Constants.KEY_ERROR to "Missing task ID"))

        val task = agentTaskRepository.getByIdOnce(taskId)
            ?: return Result.failure(workDataOf(Constants.KEY_ERROR to "Task $taskId not found"))

        // Skip already terminal or paused tasks
        if (task.status == AgentTaskStatus.COMPLETED ||
            task.status == AgentTaskStatus.PAUSED) {
            Timber.d("AgentWorker: skipping task $taskId (status=${task.status})")
            return Result.success()
        }

        // Rate limiting: don't re-run too soon for periodic tasks
        if (task.lastRunAt != null) {
            val elapsed = System.currentTimeMillis() - task.lastRunAt
            if (elapsed < Constants.AGENT_RATE_LIMIT_MS && runAttemptCount == 0) {
                Timber.d("AgentWorker: task $taskId rate-limited, requeuing with delay")
                return Result.retry()
            }
        }

        Timber.i("AgentWorker: starting task $taskId (${task.title}), attempt=$runAttemptCount")

        // Transition to RUNNING
        agentTaskRepository.updateStatus(taskId, AgentTaskStatus.RUNNING)
        agentTaskRepository.updateLastRunAt(taskId, System.currentTimeMillis())
        agentEventBus.emit(AgentEvent.Started(taskId, task.title))

        var progressNotificationId = -1

        return try {
            val executionResult = withTimeoutOrNull(Constants.MAX_AGENT_EXECUTION_MS) {
                agentDispatcher.dispatch(task) { progress, message ->
                    agentTaskRepository.updateProgress(taskId, progress)
                    agentEventBus.emit(AgentEvent.Progress(taskId, progress, message))
                    setProgress(workDataOf(
                        Constants.KEY_PROGRESS to progress,
                        Constants.KEY_PROGRESS_MESSAGE to message
                    ))
                    progressNotificationId = agentNotificationManager.showProgressNotification(
                        task.title, progress, message
                    )
                }
            }

            if (progressNotificationId != -1) {
                agentNotificationManager.cancelNotification(progressNotificationId)
            }

            when (executionResult) {
                is AgentExecutionResult.Success -> {
                    handleSuccess(task, taskId, executionResult.output)
                }
                is AgentExecutionResult.Failure -> {
                    handleFailure(task, taskId, executionResult.error, executionResult.isRetryable)
                }
                is AgentExecutionResult.Cancelled -> {
                    agentTaskRepository.updateStatus(taskId, AgentTaskStatus.PAUSED)
                    agentEventBus.emit(AgentEvent.Paused(taskId))
                    Result.success()
                }
                null -> {
                    // withTimeoutOrNull returned null → timeout
                    handleFailure(task, taskId, "Execution timed out after ${Constants.MAX_AGENT_EXECUTION_MS / 60000}min", false)
                }
            }
        } catch (e: CancellationException) {
            // Worker cancelled externally (user paused/stopped)
            if (progressNotificationId != -1) {
                agentNotificationManager.cancelNotification(progressNotificationId)
            }
            agentTaskRepository.updateStatus(taskId, AgentTaskStatus.PAUSED)
            agentEventBus.emit(AgentEvent.Paused(taskId))
            Result.success()
        } catch (e: Exception) {
            if (progressNotificationId != -1) {
                agentNotificationManager.cancelNotification(progressNotificationId)
            }
            Timber.e(e, "AgentWorker: unexpected error for task $taskId")
            handleFailure(task, taskId, e.message ?: "Unexpected error", isRetryable = true)
        }
    }

    private suspend fun handleSuccess(task: AgentTask, taskId: String, output: String): Result {
        agentTaskRepository.updateResult(taskId, output, AgentTaskStatus.COMPLETED)
        agentEventBus.emit(AgentEvent.Completed(taskId, output.take(500)))
        agentNotificationManager.showCompletionNotification(task.title, output.take(200))

        // Schedule next run for periodic tasks
        if (task.isPeriodic) {
            val intervalMs = task.intervalMinutes * 60_000L
            val nextRunAt = System.currentTimeMillis() + intervalMs
            agentTaskRepository.updateNextRunAt(taskId, nextRunAt)
            agentTaskRepository.updateStatus(taskId, AgentTaskStatus.SCHEDULED)
            agentScheduler.scheduleDelayed(task, intervalMs)
            Timber.i("AgentWorker: task $taskId scheduled for next run in ${task.intervalMinutes}min")
        }

        return Result.success(workDataOf(Constants.KEY_TASK_ID to taskId))
    }

    private suspend fun handleFailure(
        task: AgentTask,
        taskId: String,
        error: String,
        isRetryable: Boolean
    ): Result {
        val freshTask = agentTaskRepository.getByIdOnce(taskId) ?: task
        val newRetryCount = freshTask.retryCount + 1

        return if (isRetryable && newRetryCount <= task.maxRetries) {
            agentTaskRepository.updateStatus(taskId, AgentTaskStatus.RETRYING)
            agentTaskRepository.updateRetryCount(taskId, newRetryCount)
            agentEventBus.emit(AgentEvent.Retrying(taskId, newRetryCount, task.maxRetries))
            Timber.w("AgentWorker: task $taskId will retry ($newRetryCount/${task.maxRetries})")
            Result.retry()
        } else {
            agentTaskRepository.updateStatus(taskId, AgentTaskStatus.FAILED, error)
            agentEventBus.emit(AgentEvent.Failed(taskId, error))
            agentNotificationManager.showFailureNotification(task.title, error)
            Timber.e("AgentWorker: task $taskId permanently failed: $error")
            Result.failure(workDataOf(Constants.KEY_ERROR to error))
        }
    }
}
