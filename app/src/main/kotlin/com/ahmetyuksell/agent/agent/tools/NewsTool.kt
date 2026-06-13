package com.ahmetyuksell.agent.agent.tools

import com.ahmetyuksell.agent.domain.agent.Tool
import com.ahmetyuksell.agent.domain.agent.ToolResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Phase 5 — Real-world data: News headlines via newsdata.io free tier.
 * Requires NEWSDATA_API_KEY in SecureKeyStore (optional — graceful fallback).
 */
class NewsTool @Inject constructor() : Tool {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    override val name = "get_news"
    override val description = "Fetch latest news headlines on a topic or by country/category."
    override val parametersSchema = """
        {
            "type": "object",
            "properties": {
                "query": {
                    "type": "string",
                    "description": "Search query or topic, e.g. 'artificial intelligence'"
                },
                "category": {
                    "type": "string",
                    "description": "News category: technology, business, health, science, sports, entertainment"
                },
                "country": {
                    "type": "string",
                    "description": "2-letter country code e.g. 'us', 'gb', 'tr'"
                },
                "max_results": {
                    "type": "integer",
                    "description": "Maximum number of articles to return (1-10, default 5)"
                }
            },
            "required": []
        }
    """.trimIndent()

    override suspend fun execute(argsJson: String): ToolResult = withContext(Dispatchers.IO) {
        try {
            val json = Json.parseToJsonElement(argsJson).jsonObject
            val query = json["query"]?.jsonPrimitive?.content
            val category = json["category"]?.jsonPrimitive?.content
            val country = json["country"]?.jsonPrimitive?.content
            val maxResults = json["max_results"]?.jsonPrimitive?.content?.toIntOrNull()?.coerceIn(1, 10) ?: 5

            val urlBuilder = StringBuilder("https://newsdata.io/api/1/news?apikey=pub_demo")

            query?.let { urlBuilder.append("&q=${it.replace(" ", "+")}") }
            category?.let { urlBuilder.append("&category=$it") }
            country?.let { urlBuilder.append("&country=$it") }
            urlBuilder.append("&language=en")

            val request = Request.Builder().url(urlBuilder.toString()).get().build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext ToolResult.failure("News API error: ${response.code}")
            }

            val body = response.body?.string()
                ?: return@withContext ToolResult.failure("Empty news response")

            val parsed = Json { ignoreUnknownKeys = true }.parseToJsonElement(body).jsonObject
            val results = parsed["results"]?.jsonArray
                ?: return@withContext ToolResult.failure("No results in news response")

            val articles = results.take(maxResults).mapIndexed { index, item ->
                val article = item.jsonObject
                val title = article["title"]?.jsonPrimitive?.content ?: "No title"
                val description = article["description"]?.jsonPrimitive?.content?.take(200) ?: ""
                val link = article["link"]?.jsonPrimitive?.content ?: ""
                val source = article["source_id"]?.jsonPrimitive?.content ?: "Unknown"
                val pubDate = article["pubDate"]?.jsonPrimitive?.content ?: ""

                "${index + 1}. [$source] $title\n   $description\n   Published: $pubDate"
            }

            ToolResult.success("Latest news:\n\n${articles.joinToString("\n\n")}")
        } catch (e: Exception) {
            ToolResult.failure("News tool error: ${e.message}")
        }
    }
}
