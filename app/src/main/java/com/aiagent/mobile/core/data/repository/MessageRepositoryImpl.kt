package com.aiagent.mobile.core.data.repository

import com.aiagent.mobile.core.common.Constants
import com.aiagent.mobile.core.data.local.dao.ConversationDao
import com.aiagent.mobile.core.data.local.dao.MessageDao
import com.aiagent.mobile.core.data.mapper.toDomain
import com.aiagent.mobile.core.data.mapper.toEntity
import com.aiagent.mobile.core.data.preferences.EncryptedPreferencesManager
import com.aiagent.mobile.core.data.remote.api.OpenRouterApi
import com.aiagent.mobile.core.data.remote.dto.ChatMessageDto
import com.aiagent.mobile.core.data.remote.dto.ChatRequestDto
import com.aiagent.mobile.core.data.remote.dto.ChatResponseDto
import com.aiagent.mobile.core.domain.model.Message
import com.aiagent.mobile.core.domain.model.MessageRole
import com.aiagent.mobile.core.domain.model.StreamEvent
import com.aiagent.mobile.core.domain.repository.IMessageRepository
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepositoryImpl @Inject constructor(
    private val messageDao: MessageDao,
    private val conversationDao: ConversationDao,
    private val api: OpenRouterApi,
    private val prefs: EncryptedPreferencesManager,
    private val moshi: Moshi
) : IMessageRepository {

    override fun getByConversation(conversationId: String): Flow<List<Message>> =
        messageDao.getByConversation(conversationId).map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: String): Message? =
        messageDao.getById(id)?.toDomain()

    override suspend fun insert(message: Message) =
        messageDao.insert(message.toEntity())

    override suspend fun update(message: Message) =
        messageDao.update(message.toEntity())

    override suspend fun delete(messageId: String) =
        messageDao.delete(messageId)

    override fun streamMessage(
        conversationId: String,
        content: String,
        modelId: String,
        history: List<Message>
    ): Flow<StreamEvent> = flow {

        // 1 ─ Persist user message immediately
        val userMessage = Message(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            content = content.trim(),
            role = MessageRole.USER,
            timestamp = System.currentTimeMillis()
        )
        messageDao.insert(userMessage.toEntity())
        emit(StreamEvent.UserMessageSaved(userMessage))

        // 2 ─ Verify API key
        val apiKey = prefs.getApiKey()
        if (apiKey.isBlank()) {
            emit(StreamEvent.Error("OpenRouter API key is not configured. Please add it in Settings."))
            return@flow
        }

        // 3 ─ Build message history for the request (exclude system messages from history count)
        val requestMessages = (history + userMessage).map { msg ->
            ChatMessageDto(role = msg.role.apiValue, content = msg.content)
        }
        val request = ChatRequestDto(
            model = modelId,
            messages = requestMessages,
            stream = true,
            maxTokens = Constants.DEFAULT_MAX_TOKENS
        )

        // 4 ─ Call streaming endpoint
        val response = api.chatCompletionStream(request)
        if (!response.isSuccessful) {
            val rawError = response.errorBody()?.string().orEmpty()
            emit(StreamEvent.Error(parseApiError(rawError, response.code())))
            return@flow
        }

        val body = response.body() ?: run {
            emit(StreamEvent.Error("Empty response body from API"))
            return@flow
        }

        // 5 ─ Parse SSE stream line by line
        val adapter = moshi.adapter(ChatResponseDto::class.java)
        val fullContent = StringBuilder()

        body.charStream().useLines { lines ->
            for (line in lines) {
                if (!line.startsWith("data: ")) continue
                val data = line.removePrefix("data: ").trim()
                if (data == "[DONE]") break
                try {
                    val chunk = adapter.fromJson(data)
                    val chunkContent = chunk?.choices?.firstOrNull()?.delta?.content
                    if (!chunkContent.isNullOrEmpty()) {
                        fullContent.append(chunkContent)
                        emit(StreamEvent.Chunk(chunkContent))
                    }
                } catch (e: Exception) {
                    Timber.w("Skipping malformed SSE chunk: ${e.message}")
                }
            }
        }

        // 6 ─ Persist the complete AI message
        val aiMessage = Message(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            content = fullContent.toString().trim(),
            role = MessageRole.ASSISTANT,
            timestamp = System.currentTimeMillis()
        )
        messageDao.insert(aiMessage.toEntity())

        // 7 ─ Update conversation preview
        val preview = aiMessage.content.take(200).replace("\n", " ")
        conversationDao.updateLastMessage(conversationId, preview, aiMessage.timestamp)

        emit(StreamEvent.Complete(aiMessage))

    }.catch { e ->
        Timber.e(e, "streamMessage error")
        emit(StreamEvent.Error(e.message ?: "An unexpected error occurred"))
    }.flowOn(Dispatchers.IO)

    private fun parseApiError(rawError: String, code: Int): String {
        return try {
            val adapter = moshi.adapter(
                com.aiagent.mobile.core.data.remote.dto.ApiErrorDto::class.java
            )
            adapter.fromJson(rawError)?.error?.message
                ?: "API error $code"
        } catch (_: Exception) {
            "API error $code"
        }
    }
}
