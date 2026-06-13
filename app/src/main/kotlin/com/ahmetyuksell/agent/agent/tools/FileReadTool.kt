package com.ahmetyuksell.agent.agent.tools

import android.content.Context
import android.net.Uri
import com.ahmetyuksell.agent.domain.agent.Tool
import com.ahmetyuksell.agent.domain.agent.ToolResult
import com.ahmetyuksell.agent.domain.repository.FileRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

class FileReadTool @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fileRepository: FileRepository
) : Tool {

    override val name = "read_file"
    override val description = "Read the contents of an attached file by its file ID."
    override val parametersSchema = """
        {
            "type": "object",
            "properties": {
                "file_id": {
                    "type": "string",
                    "description": "The ID of the attached file to read"
                },
                "max_chars": {
                    "type": "integer",
                    "description": "Maximum characters to read (default 3000)"
                }
            },
            "required": ["file_id"]
        }
    """.trimIndent()

    override suspend fun execute(argsJson: String): ToolResult {
        return try {
            val json = Json.parseToJsonElement(argsJson).jsonObject
            val fileId = json["file_id"]?.jsonPrimitive?.content
                ?: return ToolResult.failure("Missing 'file_id' parameter")
            val maxChars = json["max_chars"]?.jsonPrimitive?.content?.toIntOrNull() ?: 3000

            val file = fileRepository.getFile(fileId)
                ?: return ToolResult.failure("File $fileId not found")

            val uri = Uri.parse(file.localUri)
            val content = context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.bufferedReader().readText().take(maxChars)
            } ?: return ToolResult.failure("Could not open file")

            ToolResult.success("File: ${file.name}\nContent:\n$content")
        } catch (e: Exception) {
            ToolResult.failure("File read error: ${e.message}")
        }
    }
}
