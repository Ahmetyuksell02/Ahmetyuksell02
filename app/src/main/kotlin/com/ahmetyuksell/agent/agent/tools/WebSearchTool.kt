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
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Phase 5 — Real-world data: Web search via DuckDuckGo Instant Answers API (no key required).
 */
class WebSearchTool @Inject constructor() : Tool {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    override val name = "web_search"
    override val description = "Search the web for information using DuckDuckGo. Returns instant answer, abstract, and related topics."
    override val parametersSchema = """
        {
            "type": "object",
            "properties": {
                "query": {
                    "type": "string",
                    "description": "The search query"
                }
            },
            "required": ["query"]
        }
    """.trimIndent()

    override suspend fun execute(argsJson: String): ToolResult = withContext(Dispatchers.IO) {
        try {
            val json = Json.parseToJsonElement(argsJson).jsonObject
            val query = json["query"]?.jsonPrimitive?.content
                ?: return@withContext ToolResult.failure("Missing 'query' parameter")

            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val url = "https://api.duckduckgo.com/?q=$encodedQuery&format=json&no_html=1&skip_disambig=1"

            val request = Request.Builder().url(url).get()
                .header("User-Agent", "AgentAI/1.0")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext ToolResult.failure("Search failed: HTTP ${response.code}")
            }

            val body = response.body?.string()
                ?: return@withContext ToolResult.failure("Empty search response")

            val parsed = Json { ignoreUnknownKeys = true }.parseToJsonElement(body).jsonObject

            val result = buildString {
                val abstract = parsed["Abstract"]?.jsonPrimitive?.content
                val instantAnswer = parsed["Answer"]?.jsonPrimitive?.content
                val abstractSource = parsed["AbstractSource"]?.jsonPrimitive?.content

                if (!instantAnswer.isNullOrBlank()) {
                    appendLine("Answer: $instantAnswer")
                }
                if (!abstract.isNullOrBlank()) {
                    appendLine("Summary (from $abstractSource): $abstract")
                }
                if (isEmpty()) {
                    appendLine("No direct answer found for: $query")
                    appendLine("Try rephrasing the query or using get_news/http_get for more specific searches.")
                }
            }

            ToolResult.success(result.trim())
        } catch (e: Exception) {
            ToolResult.failure("Web search error: ${e.message}")
        }
    }
}
