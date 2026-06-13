package com.aiagent.mobile.core.data.tool

import com.aiagent.mobile.core.domain.model.AgentTask
import com.aiagent.mobile.core.domain.tool.ToolExecutor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToolRegistry @Inject constructor(
    private val financeTool: FinanceTool,
    private val newsTool: NewsTool,
    private val webSearchTool: WebSearchTool,
    private val fileAnalysisTool: FileAnalysisTool
) {
    private val allTools: List<ToolExecutor> = listOf(
        financeTool,
        newsTool,
        webSearchTool,
        fileAnalysisTool
    )

    fun selectTools(task: AgentTask): List<ToolExecutor> =
        allTools.filter { it.activatesForTask(task) }

    fun allRegistered(): List<ToolExecutor> = allTools
}
