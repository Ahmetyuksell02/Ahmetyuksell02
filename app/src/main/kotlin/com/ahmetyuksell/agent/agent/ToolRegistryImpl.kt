package com.ahmetyuksell.agent.agent

import com.ahmetyuksell.agent.domain.agent.Tool
import com.ahmetyuksell.agent.domain.agent.ToolRegistry
import com.ahmetyuksell.agent.domain.agent.ToolResult
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToolRegistryImpl @Inject constructor(
    tools: Set<@JvmSuppressWildcards Tool>
) : ToolRegistry {

    private val toolMap: MutableMap<String, Tool> = tools.associateBy { it.name }.toMutableMap()

    init {
        Timber.d("ToolRegistry initialized with tools: ${toolMap.keys}")
    }

    override fun register(tool: Tool) {
        toolMap[tool.name] = tool
    }

    override suspend fun execute(name: String, argsJson: String): ToolResult {
        if (argsJson.length > MAX_ARGS_CHARS) {
            return ToolResult.failure(
                "Tool '$name' args too large: ${argsJson.length} chars (max $MAX_ARGS_CHARS). " +
                        "The LLM produced an oversized argument payload."
            )
        }

        val tool = toolMap[name]
            ?: return ToolResult.failure("Unknown tool: '$name'. Available: ${toolMap.keys.joinToString()}")

        return try {
            val result = withTimeout(TOOL_TIMEOUT_MS) { tool.execute(argsJson) }
            if (result.result.length > MAX_RESULT_CHARS) {
                result.copy(result = result.result.take(MAX_RESULT_CHARS) + "\n[truncated]")
            } else {
                result
            }
        } catch (e: TimeoutCancellationException) {
            Timber.e("Tool '$name' timed out after ${TOOL_TIMEOUT_MS / 1000}s")
            ToolResult.failure("Tool '$name' timed out after ${TOOL_TIMEOUT_MS / 1000}s")
        } catch (e: Exception) {
            Timber.e(e, "Tool '$name' threw exception")
            ToolResult.failure("Tool execution error: ${e.message}")
        }
    }

    override fun getToolSchemas(): List<String> = toolMap.values.map { tool ->
        val desc = tool.description.replace("\"", "\\\"").replace("\n", " ")
        """{"type":"function","function":{"name":"${tool.name}","description":"$desc","parameters":${tool.parametersSchema}}}"""
    }

    override fun getRegisteredToolNames(): List<String> = toolMap.keys.toList()

    override fun hasTool(name: String): Boolean = name in toolMap

    companion object {
        private const val MAX_RESULT_CHARS = 4000
        private const val MAX_ARGS_CHARS = 8_000
        private const val TOOL_TIMEOUT_MS = 30_000L
    }
}
