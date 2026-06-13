package com.ahmetyuksell.agent.domain.usecase.agent

import com.ahmetyuksell.agent.domain.model.AgentTask
import com.ahmetyuksell.agent.domain.model.AgentTaskStatus
import com.ahmetyuksell.agent.domain.repository.AgentTaskRepository
import com.ahmetyuksell.agent.worker.WorkManagerScheduler
import java.util.UUID
import javax.inject.Inject

class StartAgentTaskUseCase @Inject constructor(
    private val agentTaskRepository: AgentTaskRepository,
    private val workManagerScheduler: WorkManagerScheduler
) {
    suspend operator fun invoke(
        agentId: String,
        conversationId: String,
        inputText: String
    ): String {
        val task = AgentTask(
            id = UUID.randomUUID().toString(),
            agentId = agentId,
            conversationId = conversationId,
            status = AgentTaskStatus.PENDING,
            inputText = inputText
        )
        val taskId = agentTaskRepository.insertTask(task)
        workManagerScheduler.scheduleAgentTask(taskId)
        return taskId
    }
}
