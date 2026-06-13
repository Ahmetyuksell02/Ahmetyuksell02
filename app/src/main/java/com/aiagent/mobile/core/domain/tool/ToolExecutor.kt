package com.aiagent.mobile.core.domain.tool

import com.aiagent.mobile.core.domain.model.AgentTask
import com.aiagent.mobile.core.domain.model.AgentTaskType

abstract class ToolExecutor {

    abstract val toolType: ToolType

    /** Task types this tool activates for. Empty set means it never auto-activates. */
    abstract val activatesFor: Set<AgentTaskType>

    /** Execute the tool and return structured data or an error. */
    abstract suspend fun execute(task: AgentTask): ToolResult

    fun activatesForTask(task: AgentTask): Boolean = activatesFor.contains(task.taskType)
}
