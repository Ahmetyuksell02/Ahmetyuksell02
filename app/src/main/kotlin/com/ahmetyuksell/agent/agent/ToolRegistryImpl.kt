package com.ahmetyuksell.agent.agent

import com.ahmetyuksell.agent.domain.agent.Tool
import com.ahmetyuksell.agent.domain.agent.ToolRegistry
import com.ahmetyuksell.agent.domain.agent.ToolResult
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
        val tool = toolMap[name]
            ?: return ToolResult.failure("Unknown tool: '$name'. Available: ${toolMap.keys.joinToString()}")

        return try {
            val result = tool.execute(argsJson)
            if (result.result.length > MAX_RESULT_CHARS) {
                result.copy(result = result.result.take(MAX_RESULT_CHARS) + "\n[truncated]")
            } else {
                result
            }
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
    }
}
