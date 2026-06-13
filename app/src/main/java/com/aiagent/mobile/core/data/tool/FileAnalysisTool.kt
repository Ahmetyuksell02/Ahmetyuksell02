package com.aiagent.mobile.core.data.tool

import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.aiagent.mobile.core.domain.model.AgentTask
import com.aiagent.mobile.core.domain.model.AgentTaskType
import com.aiagent.mobile.core.domain.tool.ToolExecutor
import com.aiagent.mobile.core.domain.tool.ToolResult
import com.aiagent.mobile.core.domain.tool.ToolType
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileAnalysisTool @Inject constructor(
    @ApplicationContext private val context: Context
) : ToolExecutor() {

    override val toolType = ToolType.FILE_ANALYSIS
    override val activatesFor = setOf(AgentTaskType.FILE_ANALYSIS)

    // Extract URI from task prompt — format: "file://<uri>" or "content://<path>"
    private val uriPrefixes = listOf("file://", "content://", "uri:")

    override suspend fun execute(task: AgentTask): ToolResult {
        val uriString = extractUri(task.prompt)
            ?: return ToolResult.Failure(
                toolType,
                "No file URI found in task prompt. Prepend 'uri: content://...' to your prompt.",
                isFatal = true
            )

        return try {
            val uri = Uri.parse(uriString)
            val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"

            val content = when {
                mimeType == "application/pdf" || uriString.endsWith(".pdf", ignoreCase = true) ->
                    extractPdfText(uri)
                mimeType.startsWith("text/") || uriString.endsWith(".txt", ignoreCase = true)
                    || uriString.endsWith(".md", ignoreCase = true) ->
                    extractPlainText(uri)
                else ->
                    extractPlainText(uri) // try as text anyway
            }

            if (content.isNullOrBlank()) {
                ToolResult.Failure(toolType, "File is empty or could not be read", isFatal = true)
            } else {
                val truncated = if (content.length > 8000) {
                    content.take(8000) + "\n\n[File truncated — ${content.length - 8000} more characters not shown]"
                } else content

                ToolResult.Success(
                    toolType = toolType,
                    content = "File content (${content.length} chars, MIME: $mimeType):\n\n$truncated"
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "FileAnalysisTool: failed to read $uriString")
            ToolResult.Failure(toolType, "Failed to read file: ${e.message}", isFatal = true)
        }
    }

    private fun extractPlainText(uri: Uri): String? {
        return context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.bufferedReader().readText()
        }
    }

    private fun extractPdfText(uri: Uri): String? {
        val pfd: ParcelFileDescriptor = context.contentResolver
            .openFileDescriptor(uri, "r") ?: return null

        return pfd.use { fd ->
            val renderer = PdfRenderer(fd)
            val sb = StringBuilder()
            // PdfRenderer extracts rendered bitmaps — for text we render and note pages
            sb.appendLine("[PDF Document — ${renderer.pageCount} page(s)]")
            sb.appendLine("[Note: Direct text extraction from PDFs requires the PdfBox library.")
            sb.appendLine(" This analysis uses structural metadata. For full text, convert to TXT first.]")
            sb.appendLine()
            // Report basic document structure per page
            for (i in 0 until renderer.pageCount) {
                renderer.openPage(i).use { page ->
                    sb.appendLine("Page ${i + 1}: ${page.width}x${page.height}px")
                }
            }
            renderer.close()
            sb.toString()
        }
    }

    private fun extractUri(prompt: String): String? {
        for (prefix in uriPrefixes) {
            val idx = prompt.indexOf(prefix, ignoreCase = true)
            if (idx != -1) {
                return prompt.substring(idx).split(" ", "\n").firstOrNull()?.trim()
            }
        }
        return null
    }
}
