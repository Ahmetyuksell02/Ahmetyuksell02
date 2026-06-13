package com.ahmetyuksell.agent.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.ahmetyuksell.agent.domain.agent.AgentOrchestrator
import com.ahmetyuksell.agent.domain.agent.AgentResult
import com.ahmetyuksell.agent.domain.model.AgentTaskStatus
import com.ahmetyuksell.agent.domain.repository.AgentRepository
import com.ahmetyuksell.agent.domain.repository.AgentTaskRepository
import com.ahmetyuksell.agent.domain.repository.MessageRepository
import com.ahmetyuksell.agent.domain.model.Message
import com.ahmetyuksell.agent.domain.model.MessageRole
import com.ahmetyuksell.agent.notification.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.util.UUID

@HiltWorker
class AgentTaskWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val agentTaskRepository: AgentTaskRepository,
    private val agentRepository: AgentRepository,
    private val messageRepository: MessageRepository,
    private val agentOrchestrator: AgentOrchestrator,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val taskId = inputData.getString(KEY_TASK_ID)
            ?: return Result.failure()

        val task = agentTaskRepository.getTask(taskId)
            ?: return Result.failure()

        // Idempotency: WorkManager may retry; never re-execute terminal tasks
        when (task.status) {
            AgentTaskStatus.COMPLETED, AgentTaskStatus.CANCELLED -> {
                Timber.w("AgentTaskWorker: task $taskId already ${task.status}, skipping")
                return Result.success()
            }
            AgentTaskStatus.FAILED -> {
                if (runAttemptCount >= 2) {
                    Timber.w("AgentTaskWorker: task $taskId exceeded retry limit")
                    return Result.failure()
                }
            }
            else -> {}
        }

        val agent = agentRepository.getAgent(task.agentId)
            ?: return Result.failure()

        Timber.i("AgentTaskWorker: starting task $taskId (attempt $runAttemptCount)")
        agentTaskRepository.updateTaskStatus(taskId, AgentTaskStatus.RUNNING)

        setForeground(
            ForegroundInfo(
                NotificationHelper.NOTIF_AGENT_BASE + taskId.hashCode(),
                notificationHelper.buildAgentRunningNotification(agent.name, taskId)
            )
        )

        return try {
            val result = agentOrchestrator.execute(task) { step, thought ->
                setProgress(workDataOf(
                    PROGRESS_STEP to step,
                    PROGRESS_THOUGHT to thought
                ))
            }

            when (result) {
                is AgentResult.Success -> {
                    val stepsJson = Json.encodeToString(result.steps)
                    agentTaskRepository.updateTaskResult(taskId, result.answer, stepsJson)

                    messageRepository.insertMessage(
                        Message(
                            id = UUID.randomUUID().toString(),
                            conversationId = task.conversationId,
                            role = MessageRole.ASSISTANT,
                            content = "**Agent ${agent.name} completed:**\n\n${result.answer}",
                            timestamp = System.currentTimeMillis()
                        )
                    )

                    notificationHelper.showAgentCompletedNotification(
                        agent.name,
                        result.answer,
                        NotificationHelper.NOTIF_AGENT_BASE + taskId.hashCode()
                    )
                    Result.success()
                }

                is AgentResult.Failed -> {
                    agentTaskRepository.updateTaskError(taskId, result.error)
                    Timber.e("AgentTaskWorker: task $taskId failed at attempt $runAttemptCount: ${result.error}")
                    // Retry once for transient failures (network errors); the idempotency guard
                    // at the top of doWork() prevents re-executing already-COMPLETED tasks on retry.
                    if (runAttemptCount < 1) Result.retry() else Result.failure()
                }

                is AgentResult.Cancelled -> {
                    agentTaskRepository.cancelTask(taskId)
                    Timber.i("AgentTaskWorker: task $taskId cancelled")
                    Result.success()
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "AgentTaskWorker: task $taskId threw exception at attempt $runAttemptCount")
            agentTaskRepository.updateTaskError(taskId, e.message ?: "Unknown error")
            if (runAttemptCount < 1) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val KEY_TASK_ID = "task_id"
        const val PROGRESS_STEP = "step"
        const val PROGRESS_THOUGHT = "thought"
    }
}
