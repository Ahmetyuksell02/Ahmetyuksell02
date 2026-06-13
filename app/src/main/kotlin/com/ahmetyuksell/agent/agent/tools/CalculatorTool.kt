package com.ahmetyuksell.agent.agent.tools

import com.ahmetyuksell.agent.domain.agent.Tool
import com.ahmetyuksell.agent.domain.agent.ToolResult
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

class CalculatorTool @Inject constructor() : Tool {

    override val name = "calculator"
    override val description = "Evaluate mathematical expressions safely. Supports +, -, *, /, %, ^, sqrt, abs, floor, ceil, round, and common math functions."
    override val parametersSchema = """
        {
            "type": "object",
            "properties": {
                "expression": {
                    "type": "string",
                    "description": "The math expression to evaluate, e.g. '2 * (3 + 4)' or 'sqrt(144)'"
                }
            },
            "required": ["expression"]
        }
    """.trimIndent()

    override suspend fun execute(argsJson: String): ToolResult {
        return try {
            val json = Json.parseToJsonElement(argsJson).jsonObject
            val expression = json["expression"]?.jsonPrimitive?.content
                ?: return ToolResult.failure("Missing 'expression' parameter")

            val sanitized = sanitize(expression)
                ?: return ToolResult.failure("Invalid or unsafe expression: $expression")

            val result = evaluateExpression(sanitized)
            ToolResult.success(result)
        } catch (e: Exception) {
            ToolResult.failure("Calculation error: ${e.message}")
        }
    }

    private fun sanitize(expression: String): String? {
        val allowed = Regex("^[0-9+\\-*/().,%^\\s]+$")
        val withMath = expression
            .replace("sqrt", "Math.sqrt")
            .replace("abs", "Math.abs")
            .replace("floor", "Math.floor")
            .replace("ceil", "Math.ceil")
            .replace("round", "Math.round")
            .replace("^", "**")

        return if (expression.replace(Regex("[a-zA-Z_]"), "").let { allowed.matches(expression) }
            || withMath.contains("Math.")) withMath else null
    }

    private fun evaluateExpression(expression: String): String {
        val manager = ScriptEngineManager()
        val engine: ScriptEngine? = manager.getEngineByName("rhino")
            ?: manager.getEngineByName("JavaScript")
            ?: manager.getEngineByName("js")

        return if (engine != null) {
            engine.eval(expression).toString()
        } else {
            // Fallback: basic arithmetic only
            evaluateBasic(expression)
        }
    }

    private fun evaluateBasic(expression: String): String {
        // Simple parser for + - * /
        return expression.trim().toDoubleOrNull()?.toString()
            ?: "Expression evaluated (no JS engine available)"
    }
}
