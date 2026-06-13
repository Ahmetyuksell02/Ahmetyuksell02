package com.ahmetyuksell.agent.domain.agent

interface Tool {
    val name: String
    val description: String
    val parametersSchema: String
    suspend fun execute(argsJson: String): ToolResult
}

data class ToolResult(
    val success: Boolean,
    val result: String,
    val errorMessage: String? = null
) {
    companion object {
        fun success(result: String) = ToolResult(success = true, result = result)
        fun failure(error: String) = ToolResult(success = false, result = "", errorMessage = error)
    }
}
