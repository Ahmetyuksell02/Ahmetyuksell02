package com.aiagent.mobile.core.data.tool

import com.aiagent.mobile.core.domain.model.AgentTask
import com.aiagent.mobile.core.domain.model.AgentTaskType
import com.aiagent.mobile.core.domain.tool.ToolExecutor
import com.aiagent.mobile.core.domain.tool.ToolResult
import com.aiagent.mobile.core.domain.tool.ToolType
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import timber.log.Timber
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class WebSearchTool @Inject constructor(
    @Named("tool") private val httpClient: OkHttpClient
) : ToolExecutor() {

    override val toolType = ToolType.WEB_SEARCH
    override val activatesFor = setOf(
        AgentTaskType.RESEARCH,
        AgentTaskType.WEB_INVESTIGATION,
        AgentTaskType.CUSTOM
    )

    // DuckDuckGo Instant Answer API — free, no auth required
    private fun searchUrl(query: String): String {
        val encoded = URLEncoder.encode(query, "UTF-8")
        return "https://api.duckduckgo.com/?q=$encoded&format=json&no_redirect=1&no_html=1&skip_disambig=1"
    }

    override suspend fun execute(task: AgentTask): ToolResult {
        // Extract search query from task prompt (first 150 chars as query)
        val searchQuery = task.prompt.take(150).trim()

        return try {
            val json = fetch(searchUrl(searchQuery))
                ?: return ToolResult.Failure(toolType, "Web search API unavailable", isFatal = false)

            val obj = JSONObject(json)

            val content = buildString {
                val abstractText = obj.optString("AbstractText", "").trim()
                val abstractSource = obj.optString("AbstractSource", "").trim()
                val abstractUrl = obj.optString("AbstractURL", "").trim()

                if (abstractText.isNotBlank()) {
                    appendLine("**Summary (${abstractSource.ifBlank { "DuckDuckGo" }}):**")
                    appendLine(abstractText)
                    if (abstractUrl.isNotBlank()) appendLine("Source: $abstractUrl")
                    appendLine()
                }

                val relatedTopics = obj.optJSONArray("RelatedTopics")
                if (relatedTopics != null && relatedTopics.length() > 0) {
                    appendLine("**Related Topics:**")
                    val limit = minOf(relatedTopics.length(), 8)
                    for (i in 0 until limit) {
                        val topic = relatedTopics.optJSONObject(i) ?: continue
                        val text = topic.optString("Text", "").trim()
                        val url = topic.optString("FirstURL", "").trim()
                        if (text.isNotBlank()) {
                            appendLine("• $text${if (url.isNotBlank()) " [$url]" else ""}")
                        }
                    }
                }

                val answer = obj.optString("Answer", "").trim()
                if (answer.isNotBlank()) {
                    appendLine("\n**Direct Answer:**")
                    appendLine(answer)
                }
            }.trim()

            if (content.isBlank()) {
                ToolResult.Failure(toolType, "No results found for query: $searchQuery", isFatal = false)
            } else {
                ToolResult.Success(
                    toolType = toolType,
                    content = "Web search results for \"${searchQuery.take(80)}\":\n\n$content",
                    rawData = json
                )
            }
        } catch (e: Exception) {
            Timber.w(e, "WebSearchTool: search failed for query: $searchQuery")
            ToolResult.Failure(toolType, e.message ?: "Search error", isFatal = false)
        }
    }

    private fun fetch(url: String): String? {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "AiAgentMobile/1.0")
            .build()
        return httpClient.newCall(request).execute().use { response ->
            if (response.isSuccessful) response.body?.string() else null
        }
    }
}
