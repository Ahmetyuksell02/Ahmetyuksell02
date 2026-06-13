package com.aiagent.mobile.core.data.tool

import android.util.Xml
import com.aiagent.mobile.core.domain.model.AgentTask
import com.aiagent.mobile.core.domain.model.AgentTaskType
import com.aiagent.mobile.core.domain.tool.ToolExecutor
import com.aiagent.mobile.core.domain.tool.ToolResult
import com.aiagent.mobile.core.domain.tool.ToolType
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import timber.log.Timber
import java.io.StringReader
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

data class NewsItem(
    val title: String,
    val description: String,
    val source: String,
    val url: String? = null
)

@Singleton
class NewsTool @Inject constructor(
    @Named("tool") private val httpClient: OkHttpClient
) : ToolExecutor() {

    override val toolType = ToolType.NEWS
    override val activatesFor = setOf(AgentTaskType.NEWS_SUMMARY)

    // HackerNews public API
    private val hnTopStoriesUrl = "https://hacker-news.firebaseio.com/v0/topstories.json"
    private fun hnItemUrl(id: Long) = "https://hacker-news.firebaseio.com/v0/item/$id.json"

    // BBC News RSS (public, no auth)
    private val bbcRssUrl = "https://feeds.bbci.co.uk/news/rss.xml"

    override suspend fun execute(task: AgentTask): ToolResult {
        val items = mutableListOf<NewsItem>()

        // --- HackerNews ---
        try {
            val topJson = fetch(hnTopStoriesUrl)
            if (topJson != null) {
                val ids = JSONArray(topJson)
                val topIds = (0 until minOf(ids.length(), 8)).map { ids.getLong(it) }
                topIds.forEach { id ->
                    try {
                        val itemJson = fetch(hnItemUrl(id))
                        if (itemJson != null) {
                            val obj = JSONObject(itemJson)
                            val title = obj.optString("title", "").trim()
                            val url = obj.optString("url", "").takeIf { it.isNotBlank() }
                            val score = obj.optInt("score", 0)
                            if (title.isNotBlank()) {
                                items.add(NewsItem(
                                    title = title,
                                    description = "Score: $score${if (url != null) " | $url" else ""}",
                                    source = "HackerNews",
                                    url = url
                                ))
                            }
                        }
                    } catch (e: Exception) {
                        Timber.w(e, "NewsTool: failed to fetch HN item $id")
                    }
                }
            }
        } catch (e: Exception) {
            Timber.w(e, "NewsTool: HackerNews fetch failed")
        }

        // --- BBC RSS ---
        try {
            val rssXml = fetch(bbcRssUrl)
            if (rssXml != null) {
                val bbcItems = parseRss(rssXml, "BBC News", maxItems = 8)
                items.addAll(bbcItems)
            }
        } catch (e: Exception) {
            Timber.w(e, "NewsTool: BBC RSS fetch failed")
        }

        if (items.isEmpty()) {
            return ToolResult.Failure(toolType, "All news sources unavailable", isFatal = false)
        }

        // Filter by task prompt keywords if provided
        val keywords = task.prompt.lowercase().split(" ", ",", ";")
            .map { it.trim() }.filter { it.length > 3 }

        val filtered = if (keywords.isNotEmpty()) {
            val matched = items.filter { item ->
                keywords.any { kw ->
                    item.title.lowercase().contains(kw) ||
                    item.description.lowercase().contains(kw)
                }
            }.takeIf { it.isNotEmpty() } ?: items
            matched
        } else items

        val content = buildString {
            appendLine("News items fetched at ${java.util.Date()} (${filtered.size} items):")
            appendLine()
            filtered.take(12).forEachIndexed { i, item ->
                appendLine("${i + 1}. **${item.title}** [${item.source}]")
                if (item.description.isNotBlank()) appendLine("   ${item.description}")
                appendLine()
            }
        }

        return ToolResult.Success(toolType = toolType, content = content.trim())
    }

    private fun parseRss(xml: String, sourceName: String, maxItems: Int): List<NewsItem> {
        val items = mutableListOf<NewsItem>()
        try {
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(StringReader(xml))

            var eventType = parser.eventType
            var inItem = false
            var title = ""
            var description = ""
            var currentTag = ""

            while (eventType != XmlPullParser.END_DOCUMENT && items.size < maxItems) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        currentTag = parser.name
                        if (currentTag == "item") { inItem = true; title = ""; description = "" }
                    }
                    XmlPullParser.TEXT -> {
                        if (inItem) when (currentTag) {
                            "title" -> title += parser.text
                            "description" -> description += parser.text.take(200)
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name == "item" && inItem && title.isNotBlank()) {
                            items.add(NewsItem(
                                title = title.trim(),
                                description = description.trim(),
                                source = sourceName
                            ))
                            inItem = false
                        }
                        currentTag = ""
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            Timber.w(e, "NewsTool: RSS parse error for $sourceName")
        }
        return items
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
