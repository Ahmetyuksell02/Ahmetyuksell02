package com.ahmetyuksell.agent.agent.tools

import com.ahmetyuksell.agent.domain.agent.Tool
import com.ahmetyuksell.agent.domain.agent.ToolResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Phase 5 — Real-world data: Exchange rates via exchangerate-api.com (free tier).
 * No API key required for base USD conversions on free endpoint.
 */
class FinanceTool @Inject constructor() : Tool {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    override val name = "get_exchange_rate"
    override val description = "Get current currency exchange rates. Supports major currency pairs (USD, EUR, GBP, JPY, TRY, etc.)."
    override val parametersSchema = """
        {
            "type": "object",
            "properties": {
                "base": {
                    "type": "string",
                    "description": "Base currency code e.g. 'USD'"
                },
                "target": {
                    "type": "string",
                    "description": "Target currency code e.g. 'EUR'. Optional — returns all rates if omitted."
                }
            },
            "required": ["base"]
        }
    """.trimIndent()

    override suspend fun execute(argsJson: String): ToolResult = withContext(Dispatchers.IO) {
        try {
            val json = Json.parseToJsonElement(argsJson).jsonObject
            val base = json["base"]?.jsonPrimitive?.content?.uppercase()
                ?: return@withContext ToolResult.failure("Missing 'base' currency")
            val target = json["target"]?.jsonPrimitive?.content?.uppercase()

            val url = "https://api.exchangerate-api.com/v4/latest/$base"
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext ToolResult.failure("Exchange rate API error: ${response.code}")
            }

            val body = response.body?.string()
                ?: return@withContext ToolResult.failure("Empty response from exchange rate API")

            val parsed = Json { ignoreUnknownKeys = true }.parseToJsonElement(body).jsonObject
            val rates = parsed["rates"]?.jsonObject
                ?: return@withContext ToolResult.failure("No rates in response")

            val result = if (target != null) {
                val rate = rates[target]?.jsonPrimitive?.content
                    ?: return@withContext ToolResult.failure("Currency $target not found")
                "1 $base = $rate $target"
            } else {
                "Exchange rates for $base:\n" +
                        rates.entries.take(20).joinToString("\n") { (k, v) -> "  $k: ${v.jsonPrimitive.content}" }
            }

            ToolResult.success(result)
        } catch (e: Exception) {
            ToolResult.failure("Finance tool error: ${e.message}")
        }
    }
}
