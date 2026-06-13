package com.ahmetyuksell.agent.domain.agent

interface ToolRegistry {
    fun register(tool: Tool)
    suspend fun execute(name: String, argsJson: String): ToolResult
    fun getToolSchemas(): List<String>
    fun getRegisteredToolNames(): List<String>
    fun hasTool(name: String): Boolean
}
