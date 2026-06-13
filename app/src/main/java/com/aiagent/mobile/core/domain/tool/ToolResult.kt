package com.aiagent.mobile.core.domain.tool

sealed class ToolResult {
    data class Success(
        val toolType: ToolType,
        val content: String,         // pre-formatted text for LLM system prompt injection
        val rawData: String? = null, // original JSON/XML for structured access
        val fetchedAt: Long = System.currentTimeMillis()
    ) : ToolResult()

    data class Failure(
        val toolType: ToolType,
        val error: String,
        val isFatal: Boolean = false
    ) : ToolResult()
}

fun List<ToolResult>.toContextBlock(): String {
    val successes = filterIsInstance<ToolResult.Success>()
    if (successes.isEmpty()) return "No external data available."
    return successes.joinToString(separator = "\n\n---\n\n") { r ->
        "### ${r.toolType.displayName}\n${r.content}"
    }
}
