package com.ahmetyuksell.agent.domain.usecase.agent

import com.ahmetyuksell.agent.domain.repository.AgentTaskRepository
import com.ahmetyuksell.agent.worker.WorkManagerScheduler
import javax.inject.Inject

class CancelAgentTaskUseCase @Inject constructor(
    private val agentTaskRepository: AgentTaskRepository,
    private val workManagerScheduler: WorkManagerScheduler
) {
    suspend operator fun invoke(taskId: String) {
        workManagerScheduler.cancelAgentTask(taskId)
        agentTaskRepository.cancelTask(taskId)
    }
}
