package com.ahmetyuksell.agent.worker

import android.content.Context
import android.net.Uri
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.ahmetyuksell.agent.domain.model.Message
import com.ahmetyuksell.agent.domain.model.MessageRole
import com.ahmetyuksell.agent.domain.repository.FileRepository
import com.ahmetyuksell.agent.domain.repository.MessageRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.util.UUID

@HiltWorker
class FileAnalysisWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val fileRepository: FileRepository,
    private val messageRepository: MessageRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val fileId = inputData.getString(KEY_FILE_ID) ?: return Result.failure()

        val file = fileRepository.getFile(fileId) ?: run {
            Timber.w("File $fileId not found")
            return Result.failure()
        }

        return try {
            val extractedText = extractText(file.localUri, file.mimeType)

            if (extractedText.isNotBlank() && file.conversationId != null) {
                val contextMessage = Message(
                    id = UUID.randomUUID().toString(),
                    conversationId = file.conversationId,
                    role = MessageRole.SYSTEM,
                    content = "File attached: ${file.name}\n\n$extractedText",
                    timestamp = System.currentTimeMillis(),
                    fileIds = listOf(fileId)
                )
                messageRepository.insertMessage(contextMessage)
            }

            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "File analysis failed for $fileId")
            if (runAttemptCount < 1) Result.retry() else Result.failure()
        }
    }

    private fun extractText(uriString: String, mimeType: String): String {
        return try {
            val uri = Uri.parse(uriString)
            val inputStream = applicationContext.contentResolver.openInputStream(uri)
                ?: return ""

            inputStream.use { stream ->
                when {
                    mimeType.startsWith("text/") ->
                        stream.bufferedReader().readText().take(MAX_CHARS)

                    mimeType == "application/pdf" ->
                        "[PDF content — ${(stream.available() / 1024)} KB attached]"

                    mimeType.startsWith("image/") ->
                        "[Image attached — will be sent to vision model]"

                    else ->
                        stream.bufferedReader().use { it.readText().take(MAX_CHARS) }
                }
            }
        } catch (e: Exception) {
            Timber.w(e, "Text extraction failed for $uriString")
            ""
        }
    }

    companion object {
        const val KEY_FILE_ID = "file_id"
        private const val MAX_CHARS = 8000
    }
}
