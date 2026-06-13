package com.ahmetyuksell.agent.domain.usecase.file

import com.ahmetyuksell.agent.domain.model.AttachedFile
import com.ahmetyuksell.agent.domain.repository.FileRepository
import com.ahmetyuksell.agent.worker.WorkManagerScheduler
import java.util.UUID
import javax.inject.Inject

class AnalyzeFileUseCase @Inject constructor(
    private val fileRepository: FileRepository,
    private val workManagerScheduler: WorkManagerScheduler
) {
    suspend operator fun invoke(
        localUri: String,
        name: String,
        mimeType: String,
        sizeBytes: Long,
        conversationId: String?
    ): String {
        val file = AttachedFile(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            localUri = localUri,
            name = name,
            mimeType = mimeType,
            sizeBytes = sizeBytes,
            uploadedAt = System.currentTimeMillis()
        )
        val fileId = fileRepository.insertFile(file)
        workManagerScheduler.scheduleFileAnalysis(fileId)
        return fileId
    }
}
