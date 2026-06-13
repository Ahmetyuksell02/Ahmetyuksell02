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
import java.net.URL
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class HttpRequestTool @Inject constructor() : Tool {

    private val allowedDomains = setOf(
        "api.github.com",
        "api.openweathermap.org",
        "jsonplaceholder.typicode.com",
        "httpbin.org",
        "api.exchangerate-api.com",
        "newsapi.org"
    )

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    override val name = "http_get"
    override val description = "Make an HTTP GET request to an allowed API endpoint. Only whitelisted domains are permitted."
    override val parametersSchema = """
        {
            "type": "object",
            "properties": {
                "url": {
                    "type": "string",
                    "description": "The URL to fetch (must be from an allowed domain)"
                }
            },
            "required": ["url"]
        }
    """.trimIndent()

    override suspend fun execute(argsJson: String): ToolResult = withContext(Dispatchers.IO) {
        try {
            val json = Json.parseToJsonElement(argsJson).jsonObject
            val url = json["url"]?.jsonPrimitive?.content
                ?: return@withContext ToolResult.failure("Missing 'url' parameter")

            val host = URL(url).host.removePrefix("www.")
            if (host !in allowedDomains) {
                return@withContext ToolResult.failure(
                    "Domain '$host' is not in the allowed list. Allowed: $allowedDomains"
                )
            }

            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext ToolResult.failure("HTTP ${response.code}: ${response.message}")
            }

            val body = response.body?.string()?.take(3000) ?: ""
            ToolResult.success(body)
        } catch (e: Exception) {
            ToolResult.failure("HTTP request error: ${e.message}")
        }
    }
}
