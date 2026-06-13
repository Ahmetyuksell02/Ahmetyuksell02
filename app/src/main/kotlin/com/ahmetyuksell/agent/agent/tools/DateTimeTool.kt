package com.ahmetyuksell.agent.agent.tools

import com.ahmetyuksell.agent.domain.agent.Tool
import com.ahmetyuksell.agent.domain.agent.ToolResult
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

class DateTimeTool @Inject constructor() : Tool {

    override val name = "get_datetime"
    override val description = "Get current date and time information, optionally in a specific timezone."
    override val parametersSchema = """
        {
            "type": "object",
            "properties": {
                "timezone": {
                    "type": "string",
                    "description": "Timezone ID e.g. 'UTC', 'America/New_York', 'Europe/London'"
                },
                "format": {
                    "type": "string",
                    "description": "Date format e.g. 'yyyy-MM-dd HH:mm:ss'. Optional."
                }
            },
            "required": []
        }
    """.trimIndent()

    override suspend fun execute(argsJson: String): ToolResult {
        return try {
            val json = Json.parseToJsonElement(argsJson).jsonObject
            val timezoneId = json["timezone"]?.jsonPrimitive?.content ?: "UTC"
            val format = json["format"]?.jsonPrimitive?.content ?: "yyyy-MM-dd HH:mm:ss zzz"

            val tz = try {
                TimeZone.getTimeZone(timezoneId)
            } catch (e: Exception) {
                TimeZone.getTimeZone("UTC")
            }

            val sdf = SimpleDateFormat(format, Locale.getDefault()).apply {
                timeZone = tz
            }

            val now = Date()
            val result = buildString {
                appendLine("Current time: ${sdf.format(now)}")
                appendLine("Timezone: ${tz.getDisplayName(false, TimeZone.LONG)}")
                appendLine("UTC offset: ${tz.getOffset(now.time) / 3600000}h")
                appendLine("Day of week: ${SimpleDateFormat("EEEE", Locale.ENGLISH).apply { timeZone = tz }.format(now)}")
            }
            ToolResult.success(result.trim())
        } catch (e: Exception) {
            ToolResult.failure("DateTime error: ${e.message}")
        }
    }
}
